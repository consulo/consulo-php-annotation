package de.espend.idea.php.annotation.completion;

import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpAttributesList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpAttributesFQNsIndex;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionProvider;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.CompletionType;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.PrioritizedLookupElement;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.completion.insert.AnnotationTagInsertHandler;
import de.espend.idea.php.annotation.completion.insert.AttributeAliasInsertHandler;
import de.espend.idea.php.annotation.completion.lookupelements.PhpAnnotationPropertyLookupElement;
import de.espend.idea.php.annotation.completion.lookupelements.PhpClassAnnotationLookupElement;
import de.espend.idea.php.annotation.dict.AnnotationProperty;
import de.espend.idea.php.annotation.dict.AnnotationPropertyEnum;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import de.espend.idea.php.annotation.dict.PhpAnnotation;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.PhpAnnotationVirtualProperties;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationVirtualPropertyCompletionParameter;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import de.espend.idea.php.annotation.util.PhpIndexUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl(order = "first")
public class AnnotationCompletionContributor extends CompletionContributor {
    private static final Key<CachedValue<Map<String, Collection<String>>>> ATTRIBUTE_CLASSES_BY_NAMESPACE_CACHE =
        Key.create("ATTRIBUTE_CLASSES_BY_NAMESPACE_CACHE");

    private static final ElementPattern<PsiElement> DOC_IDENTIFIER_PATTERN = PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER);

    public AnnotationCompletionContributor() {
        // @<caret>
        // * @<caret>
        extend(CompletionType.BASIC, AnnotationPattern.getDocBlockTag(), new PhpDocBlockTagAnnotations());

        // #[<caret>] but only provide alias feature
        extend(CompletionType.BASIC, AnnotationPattern.getAttributeNamePattern(), new PhpAttributeAlias());

        // @Callback("", <caret>)
        extend(CompletionType.BASIC, AnnotationPattern.getDocAttribute(), new PhpDocAttributeList());
        extend(CompletionType.BASIC, AnnotationPattern.getTextIdentifier(), new PhpDocAttributeValue());
        extend(CompletionType.BASIC, AnnotationPattern.getDefaultPropertyValue(), new PhpDocDefaultValue());
        extend(CompletionType.BASIC, AnnotationPattern.getDocBlockTagAfterBackslash(), new PhpDocBlockTagAlias());

        // @Route(name=ClassName::<FOO>)
        extend(CompletionType.BASIC, AnnotationPattern.getClassConstant(), new PhpDocClassConstantCompletion());

        // @Foo(name={"FOOBAR", "<caret>"})
        extend(CompletionType.BASIC, AnnotationPattern.getPropertyArrayPattern(), new PhpDocArrayPropertyCompletion());

        // #[Route('/path', methods: ['action'])]
        extend(CompletionType.BASIC, AnnotationPattern.getAttributesArrayPattern(), new AttributesArrayPropertyCompletion());

        // #[Route('/path', methods: 'action')]
        extend(CompletionType.BASIC, AnnotationPattern.getAttributesValuePattern(), new AttributesValuePropertyCompletion());

        // #[Route('<caret>')]
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withParent(AnnotationPattern.getAttributesDefaultPattern()),
            new AttributeDefaultValue()
        );
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    private static class PhpDocDefaultValue implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
            PsiElement position = parameters.getOriginalPosition();
            if (position == null) {
                return;
            }

            PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(position, PhpDocTag.class);
            PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
            if (phpClass == null) {
                return;
            }

            AnnotationPropertyParameter property = new AnnotationPropertyParameter(position, phpClass, AnnotationPropertyParameter.Type.DEFAULT);
            providerWalker(parameters, context, result, property);
        }
    }

    private static class AttributeDefaultValue implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
            PsiElement position = parameters.getOriginalPosition();
            if (position == null) {
                return;
            }

            if (!(position.getParent() instanceof StringLiteralExpression parent)) {
                return;
            }

            PhpAttribute phpAttribute = PsiTreeUtil.getParentOfType(parent, PhpAttribute.class);
            if (phpAttribute == null) {
                return;
            }

            String fqn = phpAttribute.getFQN();
            if (fqn == null) {
                return;
            }

            PhpClass phpClass = PhpElementsUtil.getClassInterface(position.getProject(), fqn);
            if (phpClass == null) {
                return;
            }

            AnnotationPropertyParameter property = new AnnotationPropertyParameter(position, phpClass, AnnotationPropertyParameter.Type.DEFAULT);
            providerWalker(parameters, context, result, property);
        }
    }

    /**
     * "@FOO(property={"FOO", "FOO"})"
     */
    private static class PhpDocArrayPropertyCompletion implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
            PsiElement position = parameters.getOriginalPosition();
            if (position == null) {
                return;
            }

            if (!(position.getParent() instanceof StringLiteralExpression parent)) {
                return;
            }

            PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(position, PhpDocTag.class);
            PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
            if (phpClass == null) {
                return;
            }

            PsiElement propertyForEnum = AnnotationUtil.getPropertyForArray(parent);
            if (propertyForEnum == null) {
                return;
            }

            AnnotationPropertyParameter property = new AnnotationPropertyParameter(
                position,
                phpClass,
                propertyForEnum.getText(),
                AnnotationPropertyParameter.Type.PROPERTY_ARRAY
            );

            providerWalker(parameters, context, result, property);
        }
    }

    private static class AttributesArrayPropertyCompletion implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
            PsiElement position = parameters.getOriginalPosition();
            if (position == null || !(position.getParent() instanceof StringLiteralExpression)) {
                return;
            }

            ArrayCreationExpression array = PsiTreeUtil.getParentOfType(position, ArrayCreationExpression.class);
            if (array == null) {
                return;
            }

            addAttributePropertyCompletions(position, array, AnnotationPropertyParameter.Type.PROPERTY_ARRAY, parameters, context, result);
        }
    }

    private static class AttributesValuePropertyCompletion implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
            PsiElement position = parameters.getOriginalPosition();
            if (position == null || !(position.getParent() instanceof StringLiteralExpression parent)) {
                return;
            }

            addAttributePropertyCompletions(position, parent, AnnotationPropertyParameter.Type.PROPERTY_VALUE, parameters, context, result);
        }
    }

    private static void providerWalker(
        CompletionParameters parameters,
        ProcessingContext context,
        CompletionResultSet result,
        AnnotationPropertyParameter property
    ) {
        AnnotationCompletionProviderParameter completionParameter = new AnnotationCompletionProviderParameter(parameters, context, result);
        for (PhpAnnotationCompletionProvider extension : AnnotationUtil.EXTENSION_POINT_COMPLETION.getExtensionList()) {
            extension.getPropertyValueCompletions(property, completionParameter);
        }
    }

    private static class PhpDocAttributeValue implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
            PsiElement position = parameters.getOriginalPosition();
            if (position == null) {
                return;
            }

            if (!(position.getContext() instanceof StringLiteralExpression phpDocString)) {
                return;
            }

            PsiElement propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(phpDocString, DOC_IDENTIFIER_PATTERN);
            if (propertyName == null) {
                return;
            }

            PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(position, PhpDocTag.class);
            PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
            if (phpClass == null) {
                return;
            }

            AnnotationPropertyParameter property = new AnnotationPropertyParameter(
                position,
                phpClass,
                propertyName.getText(),
                AnnotationPropertyParameter.Type.PROPERTY_VALUE
            );

            providerWalker(parameters, context, result, property);
        }
    }

    /**
     * Provides attribute so field properties of annotation
     *
     * "@Foo(<caret>)" => @Foo(name=)
     * "@Foo("foo", <caret>)" => "@Foo("foo", name=)"
     */
    private static class PhpDocAttributeList implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
            PsiElement position = parameters.getOriginalPosition();
            if (position == null) {
                return;
            }

            PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(position, PhpDocTag.class);
            if (phpDocTag == null) {
                return;
            }

            PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
            if (phpClass == null) {
                return;
            }

            AnnotationUtil.visitAttributes(phpClass, (attributeName, type, target) -> {
                result.addElement(new PhpAnnotationPropertyLookupElement(
                    new AnnotationProperty(attributeName, AnnotationPropertyEnum.fromString(type))
                ));
            });

            // extension point for virtual properties
            AnnotationVirtualPropertyCompletionParameter virtualPropertyParameter = null;
            AnnotationCompletionProviderParameter completionParameter = null;

            for (PhpAnnotationVirtualProperties extension : AnnotationUtil.EP_VIRTUAL_PROPERTIES.getExtensionList()) {
                if (virtualPropertyParameter == null) {
                    virtualPropertyParameter = new AnnotationVirtualPropertyCompletionParameter(phpClass);
                }

                if (completionParameter == null) {
                    completionParameter = new AnnotationCompletionProviderParameter(parameters, context, result);
                }

                extension.addCompletions(virtualPropertyParameter, completionParameter);
            }

            if (virtualPropertyParameter == null) {
                return;
            }

            for (Map.Entry<String, AnnotationPropertyEnum> entry : virtualPropertyParameter.getLookupElements().entrySet()) {
                result.addElement(new PhpAnnotationPropertyLookupElement(new AnnotationProperty(entry.getKey(), entry.getValue())));
            }
        }
    }

    /**
     * Extends attribute completion, but only for alias
     */
    private static class PhpAttributeAlias implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
            PsiElement position = parameters.getOriginalPosition();
            if (position == null) {
                return;
            }

            PhpAttributesList attributesList = PsiTreeUtil.getParentOfType(position, PhpAttributesList.class);
            if (attributesList == null) {
                return;
            }

            attachLookupElements(position.getProject(), attributesList, result);
        }

        private void attachLookupElements(Project project, PhpAttributesList attributesList, CompletionResultSet result) {
            Map<String, String> items = new HashMap<>();
            for (UseAliasOption option : ApplicationSettings.getUseAliasOptionsWithDefaultFallback()) {
                String alias = option.getAlias();
                String className = option.getClassName();
                if (alias == null || className == null) {
                    continue;
                }
                items.put(alias, className);
            }

            items.putAll(getUseAsMap(attributesList));

            Map<String, Collection<String>> attributesByNamespace = getAttributeClassesByNamespace(project);
            for (Map.Entry<String, String> entry : items.entrySet()) {
                String alias = entry.getKey();
                String aliasFqn = entry.getValue();

                String namespace = "\\" + StringUtils.stripStart(aliasFqn, "\\");
                Collection<String> classNames = attributesByNamespace.get(namespace);
                if (classNames == null) {
                    continue;
                }

                for (String fqnClass : classNames) {
                    String lookupString = alias + "\\" + fqnClass.substring(namespace.length() + 1);
                    PhpClass underlyingClass = PhpElementsUtil.getClassInterface(project, fqnClass);
                    if (underlyingClass == null) {
                        continue;
                    }

                    // check if Attribute is target allowed for context
                    // @see com.jetbrains.php.completion.PhpCompletionContributor.PhpClassRefCompletionProvider.shouldAddElement
                    if (isApplicableAttributeClass(underlyingClass, attributesList)) {
                        PhpClassAnnotationLookupElement lookupElement = new PhpClassAnnotationLookupElement(
                            underlyingClass,
                            new UseAliasOption(aliasFqn, alias, true),
                            lookupString
                        );

                        lookupElement.withInsertHandler(AttributeAliasInsertHandler.getInstance());
                        result.addElement(withDeprecatedPriority(lookupElement, underlyingClass));
                    }
                }
            }
        }

        /**
         * Local replacement for PhpStorm's
         * {@code PhpClassCantBeUsedAsAttributeInspection.rootAttributes(PhpClass)} and
         * {@code PhpInapplicableAttributeTargetDeclarationInspection.getInapplicableDeclarationName(PsiElement, List)}.
         */
        private static boolean isApplicableAttributeClass(PhpClass phpClass, PhpAttributesList attributesList) {
            // TODO consulo-php: PhpClassCantBeUsedAsAttributeInspection#rootAttributes missing; only direct "#[\Attribute]" is checked
            // TODO consulo-php: PhpInapplicableAttributeTargetDeclarationInspection#getInapplicableDeclarationName missing; target flags are not validated
            return !phpClass.getAttributes("\\Attribute").isEmpty();
        }
    }

    private static class PhpDocBlockTagAnnotations implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
            PsiElement position = parameters.getOriginalPosition();
            if (position == null) {
                return;
            }

            PhpDocComment docComment = PsiTreeUtil.getParentOfType(position, PhpDocComment.class);
            if (docComment == null) {
                return;
            }

            AnnotationTarget annotationTarget = PhpElementsUtil.findAnnotationTarget(docComment);
            if (annotationTarget == null) {
                return;
            }

            Map<String, String> importMap = AnnotationUtil.getUseImportMap((PsiElement) docComment);
            attachLookupElements(parameters.getPosition().getProject(), importMap, annotationTarget, result);
        }

        private void attachLookupElements(Project project, Map<String, String> importMap, AnnotationTarget target, CompletionResultSet result) {
            for (PhpAnnotation annotation : getPhpAnnotationTargetClasses(project, target)) {
                PhpClass underlyingClass = annotation.getPhpClass();

                String fqnClass = underlyingClass.getFQN();
                if (!fqnClass.startsWith("\\")) {
                    fqnClass = "\\" + fqnClass;
                }

                PhpClassAnnotationLookupElement lookupElement = new PhpClassAnnotationLookupElement(underlyingClass)
                    .withInsertHandler(AnnotationTagInsertHandler.getInstance());

                for (Map.Entry<String, String> entry : importMap.entrySet()) {
                    String importedFqn = entry.getValue();
                    if (fqnClass.startsWith(importedFqn + "\\")) {
                        lookupElement.withTypeText(entry.getKey() + fqnClass.substring(importedFqn.length()));
                    }
                }

                // attach class also "@ORM\Entity" if there is not import but an alias via settings
                for (UseAliasOption option : ApplicationSettings.getUseAliasOptionsWithDefaultFallback()) {
                    String className = option.getClassName();
                    String alias = option.getAlias();
                    if (className == null || alias == null) {
                        continue;
                    }

                    String namespace = "\\" + StringUtils.stripStart(className, "\\") + "\\";
                    if (!fqnClass.startsWith(namespace)) {
                        continue;
                    }

                    PhpClassAnnotationLookupElement aliasLookup = new PhpClassAnnotationLookupElement(
                        underlyingClass,
                        option,
                        alias + "\\" + fqnClass.substring(namespace.length())
                    );

                    aliasLookup.withInsertHandler(AnnotationTagInsertHandler.getInstance());
                    result.addElement(withDeprecatedPriority(aliasLookup, underlyingClass));
                }

                result.addElement(withDeprecatedPriority(lookupElement, underlyingClass));
            }
        }

        private Collection<PhpAnnotation> getPhpAnnotationTargetClasses(Project project, AnnotationTarget target) {
            // @TODO: how handle unknown types
            return AnnotationUtil.getAnnotationsOnTargetMap(
                project,
                target,
                AnnotationTarget.ALL,
                AnnotationTarget.UNKNOWN,
                AnnotationTarget.UNDEFINED
            ).values();
        }
    }

    private static class PhpDocBlockTagAlias implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
            PsiElement position = parameters.getOriginalPosition();
            if (position == null) {
                return;
            }

            if (!(position.getParent() instanceof PhpDocTag phpDocTag)) {
                return;
            }

            String tagName = phpDocTag.getName();
            if (!tagName.startsWith("@")) {
                return;
            }

            int separator = tagName.indexOf("\\");
            if (separator == -1) {
                return;
            }

            String name = tagName.substring(1, separator);

            Map<String, String> importMap = AnnotationUtil.getUseImportMap((PsiElement) phpDocTag);
            String importedNamespace = importMap.get(name);
            if (importedNamespace == null) {
                return;
            }

            // find annotation scope, to filter classes
            AnnotationTarget annotationTarget = PhpElementsUtil.findAnnotationTarget(PsiTreeUtil.getParentOfType(position, PhpDocComment.class));
            if (annotationTarget == null) {
                annotationTarget = AnnotationTarget.UNKNOWN;
            }

            // force trailing backslash on namespace
            String namespace = importedNamespace.startsWith("\\") ? importedNamespace : "\\" + importedNamespace;

            Map<String, PhpAnnotation> annotations = AnnotationUtil.getAnnotationsOnTargetMap(
                position.getProject(),
                AnnotationTarget.ALL,
                AnnotationTarget.UNDEFINED,
                AnnotationTarget.UNKNOWN,
                annotationTarget
            );

            for (PhpClass phpClass : PhpIndexUtil.getPhpClassInsideNamespace(position.getProject(), namespace)) {
                String fqnName = StringUtils.stripStart(phpClass.getFQN(), "\\");
                PhpAnnotation annotation = annotations.get(fqnName);
                if (annotation != null && annotation.matchOneOf(
                    AnnotationTarget.ALL,
                    AnnotationTarget.UNDEFINED,
                    AnnotationTarget.UNKNOWN,
                    annotationTarget
                )) {
                    String lookupString = name + "\\" + fqnName.substring(namespace.length());
                    result.addElement(
                        LookupElementBuilder.create(lookupString)
                            .withTypeText(phpClass.getPresentableFQN(), true)
                            .withIcon(IconDescriptorUpdaters.getIcon(phpClass, 0))
                            .withInsertHandler(AnnotationTagInsertHandler.getInstance())
                    );
                }
            }
        }
    }

    /**
     * Completion for const fields inside phpdoc @Route(name=ClassName::<FOO>)
     */
    private static class PhpDocClassConstantCompletion implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
            PsiElement position = parameters.getOriginalPosition();
            if (position == null) {
                return;
            }

            PhpClass phpClass = AnnotationUtil.getClassFromConstant(position);
            if (phpClass == null) {
                return;
            }

            for (Field field : phpClass.getFields()) {
                if (field.isConstant()) {
                    result.addElement(
                        LookupElementBuilder.create(field.getName())
                            .withIcon(PhpIcons.FIELD)
                            .withTypeText(phpClass.getName(), true)
                    );
                }
            }
        }
    }

    private static LookupElement withDeprecatedPriority(LookupElement lookupElement, PhpClass phpClass) {
        if (phpClass.isDeprecated()) {
            return PrioritizedLookupElement.withPriority(lookupElement, -1000.0);
        }

        return lookupElement;
    }

    @RequiredReadAction
    private static void addAttributePropertyCompletions(
        PsiElement position,
        PsiElement attributeNameOwner,
        AnnotationPropertyParameter.Type type,
        CompletionParameters parameters,
        ProcessingContext context,
        CompletionResultSet result
    ) {
        PsiElement attributeNamePsi = PhpPsiUtil.getPrevSibling(
            attributeNameOwner,
            sibling -> sibling instanceof PsiWhiteSpace || sibling.getNode().getElementType() == PhpTokenTypes.opCOLON
        );

        if (attributeNamePsi == null || attributeNamePsi.getNode().getElementType() != PhpTokenTypes.IDENTIFIER) {
            return;
        }

        String attributeName = attributeNamePsi.getText();
        if (StringUtils.isBlank(attributeName)) {
            return;
        }

        PhpAttribute phpAttribute = PsiTreeUtil.getParentOfType(position, PhpAttribute.class);
        if (phpAttribute == null) {
            return;
        }

        String fqn = phpAttribute.getFQN();
        if (fqn == null) {
            return;
        }

        PhpClass phpClass = PhpElementsUtil.getClassInterface(position.getProject(), fqn);
        if (phpClass == null) {
            return;
        }

        AnnotationCompletionProviderParameter completionParameter = new AnnotationCompletionProviderParameter(parameters, context, result);
        AnnotationPropertyParameter property = new AnnotationPropertyParameter(position, phpClass, attributeName, type);

        for (PhpAnnotationCompletionProvider extension : AnnotationUtil.EXTENSION_POINT_COMPLETION.getExtensionList()) {
            extension.getPropertyValueCompletions(property, completionParameter);
        }
    }

    /**
     * Cache attribute FQNs by namespace prefix so alias completion can query only relevant branches.
     * <p>
     * Example key: "\Doctrine\ORM\Mapping" => ["\Doctrine\ORM\Mapping\Entity", ...]
     */
    private static Map<String, Collection<String>> getAttributeClassesByNamespace(Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            ATTRIBUTE_CLASSES_BY_NAMESPACE_CACHE,
            () -> {
                Map<String, List<String>> items = new HashMap<>();
                for (String indexedFqn : FileBasedIndex.getInstance().getAllKeys(PhpAttributesFQNsIndex.KEY, project)) {
                    String fqnClass = indexedFqn.startsWith("\\") ? indexedFqn : "\\" + indexedFqn;

                    int index = fqnClass.indexOf("\\", 1);
                    while (index > 0) {
                        items.computeIfAbsent(fqnClass.substring(0, index), k -> new ArrayList<>()).add(fqnClass);
                        index = fqnClass.indexOf("\\", index + 1);
                    }
                }

                Map<String, Collection<String>> immutableItems = new HashMap<>();
                for (Map.Entry<String, List<String>> entry : items.entrySet()) {
                    immutableItems.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
                }

                return CachedValueProvider.Result.create(
                    Collections.unmodifiableMap(immutableItems),
                    AnnotationUtil.getModificationTrackerForIndexId(project, PhpAttributesFQNsIndex.KEY)
                );
            },
            false
        );
    }

    private static Map<String, String> getUseAsMap(PsiElement element) {
        PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(element);
        if (scope == null) {
            return Collections.emptyMap();
        }

        Map<String, String> imports = new HashMap<>();
        for (PhpUseList useList : PhpCodeInsightUtil.collectImports(scope)) {
            for (PhpUse use : useList.getDeclarations()) {
                String alias = use.getAliasName();
                if (alias == null) {
                    continue;
                }
                imports.put(alias, use.getFQN());
            }
        }

        return imports;
    }
}
