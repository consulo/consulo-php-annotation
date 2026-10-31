package de.espend.idea.php.annotation.reference;

import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.PsiReferenceRegistrar;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationPropertyValueReferenceContributor extends PsiReferenceContributor {
    private static final ElementPattern<PsiElement> DOC_IDENTIFIER_PATTERN = PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER);

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(AnnotationPattern.getDefaultPropertyValueString(), new PropertyValueDefaultReferences());
        registrar.registerReferenceProvider(AnnotationPattern.getPropertyValueString(), new PropertyValueReferences());

        // #[Route('/path', name: '<caret>')]
        registrar.registerReferenceProvider(AnnotationPattern.getAttributesValueReferencesPattern(), new AttributeValueReferences());

        // #[Route('<caret>')]
        registrar.registerReferenceProvider(AnnotationPattern.getAttributesDefaultPattern(), new AttributeDefaultReferences());
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    /**
     * '@Template("foo.twig.html")'
     * '@Service("foo")'
     */
    private static class PropertyValueDefaultReferences extends PsiReferenceProvider {
        @Override
        public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
            PhpClass phpClass = getValidAnnotationClass(element);
            if (phpClass == null) {
                return PsiReference.EMPTY_ARRAY;
            }

            AnnotationPropertyParameter property = new AnnotationPropertyParameter(element, phpClass, AnnotationPropertyParameter.Type.DEFAULT);
            return addPsiReferences(element, context, property);
        }
    }

    /**
     * '@Template(name="foo.twig.html")'
     */
    private static class PropertyValueReferences extends PsiReferenceProvider {
        @Override
        public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
            PhpClass phpClass = getValidAnnotationClass(element);
            if (phpClass == null) {
                return PsiReference.EMPTY_ARRAY;
            }

            PsiElement propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(element, DOC_IDENTIFIER_PATTERN);
            if (propertyName == null) {
                return PsiReference.EMPTY_ARRAY;
            }

            AnnotationPropertyParameter property = new AnnotationPropertyParameter(
                element,
                phpClass,
                propertyName.getText(),
                AnnotationPropertyParameter.Type.PROPERTY_VALUE
            );

            return addPsiReferences(element, context, property);
        }
    }

    /**
     * #[Route('/path', name: '<caret>')]
     */
    private static class AttributeValueReferences extends PsiReferenceProvider {
        @Override
        public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
            PhpClass phpClass = getAttributeClass(element);
            if (phpClass == null) {
                return PsiReference.EMPTY_ARRAY;
            }

            PsiElement attributeNamePsi = PhpPsiUtil.getPrevSibling(
                element,
                sibling -> sibling instanceof PsiWhiteSpace || sibling.getNode().getElementType() == PhpTokenTypes.opCOLON
            );

            if (attributeNamePsi == null || attributeNamePsi.getNode().getElementType() != PhpTokenTypes.IDENTIFIER) {
                return PsiReference.EMPTY_ARRAY;
            }

            String attributeName = attributeNamePsi.getText();
            if (StringUtils.isBlank(attributeName)) {
                return PsiReference.EMPTY_ARRAY;
            }

            AnnotationPropertyParameter property = new AnnotationPropertyParameter(
                element,
                phpClass,
                attributeName,
                AnnotationPropertyParameter.Type.PROPERTY_VALUE
            );

            return addPsiReferences(element, context, property);
        }
    }

    /**
     * #[Route('<caret>')]
     */
    private static class AttributeDefaultReferences extends PsiReferenceProvider {
        @Override
        public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
            PhpClass phpClass = getAttributeClass(element);
            if (phpClass == null) {
                return PsiReference.EMPTY_ARRAY;
            }

            AnnotationPropertyParameter property = new AnnotationPropertyParameter(element, phpClass, AnnotationPropertyParameter.Type.DEFAULT);
            return addPsiReferences(element, context, property);
        }
    }

    private static @Nullable PhpClass getValidAnnotationClass(PsiElement element) {
        PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(element, PhpDocTag.class);
        if (phpDocTag == null) {
            return null;
        }

        return AnnotationUtil.getAnnotationReference(phpDocTag);
    }

    private static PsiReference[] addPsiReferences(PsiElement element, ProcessingContext context, AnnotationPropertyParameter property) {
        List<PsiReference> references = new ArrayList<>();

        PhpAnnotationReferenceProviderParameter parameter = new PhpAnnotationReferenceProviderParameter(element, context);
        for (PhpAnnotationReferenceProvider extension : AnnotationUtil.EXTENSION_POINT_REFERENCES.getExtensionList()) {
            PsiReference[] extensionReferences = extension.getPropertyReferences(property, parameter);
            if (extensionReferences == null) {
                continue;
            }

            for (PsiReference reference : extensionReferences) {
                if (reference != null) {
                    references.add(reference);
                }
            }
        }

        return references.toArray(PsiReference.EMPTY_ARRAY);
    }

    private static @Nullable PhpClass getAttributeClass(PsiElement element) {
        if (!(element instanceof StringLiteralExpression)) {
            return null;
        }

        PhpAttribute phpAttribute = PsiTreeUtil.getParentOfType(element, PhpAttribute.class);
        if (phpAttribute == null) {
            return null;
        }

        String fqn = phpAttribute.getFQN();
        if (fqn == null) {
            return null;
        }

        return PhpElementsUtil.getClassInterface(element.getProject(), fqn);
    }
}
