package de.espend.idea.php.annotation.inspection;

import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
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
 * Provide inpsection check for the class of "foo=Foo\Foo::cla<caret>ss"
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationDocBlockClassConstantNotFoundInspection extends AnnotationLocalInspectionTool {
    public static final String MESSAGE = "[Annotations] Class not found";

    public AnnotationDocBlockClassConstantNotFoundInspection() {
        super("AnnotationDocBlockClassConstantNotFound", "[Annotations] Class not found");
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
            if (nextSibling == null
                || nextSibling.getNode().getElementType() != PhpDocTokenTypes.DOC_IDENTIFIER
                || !"class".equals(nextSibling.getText())) {
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

            PhpClass classInterface = PhpElementsUtil.getClassInterface(phpDocTag.getProject(), clazz);
            if (classInterface == null) {
                holder.newProblem(LocalizeValue.of(MESSAGE))
                    .range(nextSibling)
                    .highlightType(ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
                    .create();
            }
        }
    }
}
