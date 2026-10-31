package de.espend.idea.php.annotation.inspection;

import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import de.espend.idea.php.annotation.inspection.visitor.PhpDocTagWithUsePsiElementVisitor;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;

/**
 * Attach constant deprecated also includes "::class"
 * <p>
 * - "@FOO(test=Test::cl<caret>ass)"
 * - "@FOO(test=Test::VERS<caret>ION)"
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationDocBlockConstantDeprecatedInspection extends AnnotationLocalInspectionTool {
    public static final String MESSAGE = "[Annotations] Deprecated usage";

    public AnnotationDocBlockConstantDeprecatedInspection() {
        super("AnnotationDocBlockConstantDeprecated", "[Annotations] Deprecated usage");
    }

    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpDocTagWithUsePsiElementVisitor(holder, this::visitAnnotationDocTag);
    }

    private void visitAnnotationDocTag(
        PhpDocTag phpDocTag,
        ProblemsHolder holder,
        AnnotationInspectionUtil.LazyNamespaceImportResolver lazyUseImporterCollector
    ) {
        for (PsiElement element : PsiTreeUtil.collectElements(phpDocTag, e -> e.getNode().getElementType() == PhpDocTokenTypes.DOC_STATIC)) {
            PsiElement nextSibling = element.getNextSibling();
            if (nextSibling == null || nextSibling.getNode().getElementType() != PhpDocTokenTypes.DOC_IDENTIFIER) {
                continue;
            }

            PsiElement prevSibling = element.getPrevSibling();
            if (prevSibling == null) {
                return;
            }

            String namespaceForDocIdentifier = PhpDocUtil.getNamespaceForDocIdentifier(prevSibling);
            if (namespaceForDocIdentifier == null) {
                return;
            }

            String clazz = AnnotationInspectionUtil.getClassFqnString(namespaceForDocIdentifier, lazyUseImporterCollector);
            if (clazz == null) {
                return;
            }

            PhpClass phpClass = PhpElementsUtil.getClassInterface(phpDocTag.getProject(), clazz);
            if (phpClass == null) {
                return;
            }

            // ::class direct class access
            String text = nextSibling.getText();
            if ("class".equals(text)) {
                if (phpClass.isDeprecated()) {
                    registerDeprecated(holder, nextSibling);
                }

                return;
            }

            // ::CONST fetch the field
            Field fieldByName = phpClass.findFieldByName(text, true);
            if (fieldByName != null && fieldByName.isConstant() && fieldByName.isDeprecated()) {
                registerDeprecated(holder, nextSibling);
            }
        }
    }

    private static void registerDeprecated(ProblemsHolder holder, PsiElement target) {
        holder.newProblem(LocalizeValue.of(MESSAGE))
            .range(target)
            .highlightType(ProblemHighlightType.LIKE_DEPRECATED)
            .create();
    }
}
