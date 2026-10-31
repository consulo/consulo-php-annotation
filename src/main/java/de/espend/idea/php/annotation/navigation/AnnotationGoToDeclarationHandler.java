package de.espend.idea.php.annotation.navigation;

import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.navigation.GotoDeclarationHandler;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagGotoHandler;
import de.espend.idea.php.annotation.extension.PhpAnnotationVirtualProperties;
import de.espend.idea.php.annotation.extension.parameter.AnnotationDocTagGotoHandlerParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationVirtualPropertyTargetsParameter;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationGoToDeclarationHandler implements GotoDeclarationHandler {
    // <@Test>, <@Test\Test>
    private static final ElementPattern<PsiElement> DOC_TAG_NAME_PATTERN = PlatformPatterns.psiElement(PhpDocElementTypes.DOC_TAG_NAME)
        .withText(StandardPatterns.string().startsWith("@"))
        .withLanguage(PhpLanguage.INSTANCE);

    // @Route(name=<ClassName>::FOO)
    private static final ElementPattern<PsiElement> DOC_IDENTIFIER_BEFORE_STATIC_PATTERN = PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)
        .beforeLeaf(AnnotationPattern.getDocStaticPattern())
        .withLanguage(PhpLanguage.INSTANCE);

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement == null) {
            return PsiElement.EMPTY_ARRAY;
        }

        List<PsiElement> targets = new ArrayList<>();

        // @Test(<foo>=)
        if (AnnotationPattern.getDocAttribute().accepts(sourceElement)) {
            addPropertyGoto(sourceElement, targets);
        }

        // <@Test>
        // <@Test\Test>
        if (DOC_TAG_NAME_PATTERN.accepts(sourceElement)) {
            addDocTagNameGoto(sourceElement, targets);
        }

        // @Route(name=<ClassName>::FOO)
        if (DOC_IDENTIFIER_BEFORE_STATIC_PATTERN.accepts(sourceElement)) {
            addStaticClassTargets(sourceElement, targets);
        }

        // @Route(name=ClassName::<FOO>)
        if (AnnotationPattern.getClassConstant().accepts(sourceElement)) {
            addStaticClassConstTargets(sourceElement, targets);
        }

        return targets.toArray(PsiElement.EMPTY_ARRAY);
    }

    /**
     * Add goto for DocTag itself which should be the PhpClass and provide Extension
     *
     * @param psiElement origin DOC_TAG_NAME psi element
     * @param targets    Goto targets
     */
    private void addDocTagNameGoto(PsiElement psiElement, List<PsiElement> targets) {
        if (!(psiElement.getContext() instanceof PhpDocTag phpDocTag)) {
            return;
        }

        PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
        if (phpClass == null) {
            return;
        }

        targets.add(phpClass);

        AnnotationDocTagGotoHandlerParameter parameter = new AnnotationDocTagGotoHandlerParameter(phpDocTag, phpClass, targets);
        for (PhpAnnotationDocTagGotoHandler extension : AnnotationUtil.EP_DOC_TAG_GOTO.getExtensionList()) {
            extension.getGotoDeclarationTargets(parameter);
        }
    }

    /**
     * Add goto for property value which are Fields inside PhpClass
     *
     * @param psiElement origin DOC_IDENTIFIER psi element
     * @param targets    Goto targets
     */
    private void addPropertyGoto(PsiElement psiElement, List<PsiElement> targets) {
        PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(psiElement, PhpDocTag.class);
        if (phpDocTag == null) {
            return;
        }

        PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
        if (phpClass == null) {
            return;
        }

        String property = psiElement.getText();
        if (StringUtils.isBlank(property)) {
            return;
        }

        AnnotationUtil.visitAttributes(phpClass, (attributeName, type, target) -> {
            if (property.equals(attributeName)) {
                targets.add(target);
            }
        });

        // extension point to provide virtual properties / fields targets
        AnnotationVirtualPropertyTargetsParameter parameter = null;
        for (PhpAnnotationVirtualProperties extension : AnnotationUtil.EP_VIRTUAL_PROPERTIES.getExtensionList()) {
            if (parameter == null) {
                parameter = new AnnotationVirtualPropertyTargetsParameter(phpClass, psiElement, property);
            }

            extension.getTargets(parameter);
        }

        if (parameter != null) {
            targets.addAll(parameter.getTargets());
        }
    }

    /**
     * Add class targets @Route(name=<ClassName>::FOO)
     *
     * @param psiElement DOC_IDENTIFIER
     * @param targets    Goto targets
     */
    private void addStaticClassTargets(PsiElement psiElement, List<PsiElement> targets) {
        PhpClass phpClass = AnnotationUtil.getClassFromDocIdentifier(psiElement);
        if (phpClass != null) {
            targets.add(phpClass);
        }
    }

    /**
     * Add static field targets for @Route(name=ClassName::<FOO>)
     *
     * @param psiElement DOC_IDENTIFIER after DOC_STATIC
     * @param targets    Goto targets
     */
    private void addStaticClassConstTargets(PsiElement psiElement, List<PsiElement> targets) {
        PhpClass phpClass = AnnotationUtil.getClassFromConstant(psiElement);
        if (phpClass == null) {
            return;
        }

        String constName = psiElement.getText();
        if ("class".equals(constName)) {
            targets.add(phpClass);
            return;
        }

        Field field = phpClass.findFieldByName(constName, true);
        if (field != null) {
            targets.add(field);
        }
    }
}
