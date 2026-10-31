package de.espend.idea.php.annotation.doctrine.action;

import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.annotation.component.ActionRefAnchor;
import consulo.codeEditor.Editor;
import consulo.language.editor.action.CodeInsightAction;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.ex.action.IdeActions;
import de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention;
import de.espend.idea.php.annotation.icon.PhpAnnotationIconGroup;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ActionImpl(
    id = "PhpAnnotation.Doctrine.Orm.DoctrineAddRepositoryGenerateAction",
    parents = @ActionParentRef(value = @ActionRef(id = IdeActions.GROUP_GENERATE), anchor = ActionRefAnchor.LAST)
)
public class DoctrineAddRepositoryGenerateAction extends CodeInsightAction {
    private static final ElementPattern<PsiElement> INSIDE_PHP_CLASS_PATTERN = PlatformPatterns.psiElement().inside(PhpClass.class);
    private static final ElementPattern<PsiElement> INSIDE_PHP_DOC_COMMENT_PATTERN = PlatformPatterns.psiElement().inside(PhpDocComment.class);

    public DoctrineAddRepositoryGenerateAction() {
        super(LocalizeValue.localizeTODO("Add Entity Repository"), LocalizeValue.empty(), PhpAnnotationIconGroup.doctrine());
    }

    @Override
    protected CodeInsightActionHandler getHandler() {
        return (project, editor, file) -> {
            PhpClass phpClass = getPhpClassOnValidScope(editor, file);
            if (phpClass == null) {
                return;
            }

            PsiElement firstChild = phpClass.getFirstChild();
            if (firstChild != null) {
                new DoctrineOrmRepositoryIntention().invoke(project, editor, firstChild);
            }
        };
    }

    @Override
    protected boolean isValidForFile(Project project, Editor editor, PsiFile file) {
        PhpClass phpClass = getPhpClassOnValidScope(editor, file);
        if (phpClass == null) {
            return false;
        }

        PsiElement firstChild = phpClass.getFirstChild();
        return firstChild != null && new DoctrineOrmRepositoryIntention().isAvailable(project, editor, firstChild);
    }

    private static @Nullable PhpClass getPhpClassOnValidScope(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        if (offset <= 0) {
            return null;
        }

        PsiElement psiElement = file.findElementAt(offset);
        if (psiElement == null) {
            return null;
        }

        // attribute and direct hit
        if (INSIDE_PHP_CLASS_PATTERN.accepts(psiElement)) {
            return PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
        }

        // docblock are outside the phpclass scope
        if (INSIDE_PHP_DOC_COMMENT_PATTERN.accepts(psiElement)) {
            PhpDocComment docComment = PsiTreeUtil.getParentOfType(psiElement, PhpDocComment.class);
            if (docComment != null && docComment.getNextPsiSibling() instanceof PhpClass phpClass) {
                return phpClass;
            }
        }

        return PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
    }
}
