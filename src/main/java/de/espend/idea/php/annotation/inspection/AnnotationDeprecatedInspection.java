package de.espend.idea.php.annotation.inspection;

import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import de.espend.idea.php.annotation.util.AnnotationUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationDeprecatedInspection extends AnnotationLocalInspectionTool {
    public static final String MESSAGE = "[Annotations] Annotation is deprecated";

    public AnnotationDeprecatedInspection() {
        super("AnnotationDeprecatedInspection", "[Annotations] Annotation is deprecated");
    }

    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (element instanceof PhpDocTag phpDocTag && AnnotationUtil.isAnnotationPhpDocTag(phpDocTag)) {
                    visitAnnotationDocTag(phpDocTag, holder);
                }

                super.visitElement(element);
            }
        };
    }

    private void visitAnnotationDocTag(PhpDocTag phpDocTag, ProblemsHolder holder) {
        PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
        if (phpClass == null || !phpClass.isDeprecated()) {
            return;
        }

        PsiElement firstChild = phpDocTag.getFirstChild();
        if (firstChild == null || firstChild.getNode().getElementType() != PhpDocElementTypes.DOC_TAG_NAME) {
            return;
        }

        holder.newProblem(LocalizeValue.of(MESSAGE))
            .range(firstChild)
            .highlightType(ProblemHighlightType.LIKE_DEPRECATED)
            .create();
    }
}
