package de.espend.idea.php.annotation.doctrine.action;

import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.annotation.component.ActionRefAnchor;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.ui.ex.action.IdeActions;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ActionImpl(
    id = "PhpAnnotation.Doctrine.Embedded.ClassGenerator",
    parents = @ActionParentRef(value = @ActionRef(id = IdeActions.GROUP_GENERATE), anchor = ActionRefAnchor.LAST)
)
public class DoctrineEmbeddedClassAnnotationGenerateAction extends DoctrineClassGeneratorAction {
    public DoctrineEmbeddedClassAnnotationGenerateAction() {
        super("Embedded Class");
    }

    @Override
    protected String supportedClass() {
        return "Doctrine\\ORM\\Mapping\\Embeddable";
    }

    @Override
    protected void execute(Editor editor, PhpClass phpClass, PsiFile psiFile) {
        // insert ORM alias
        PsiElement firstChild = phpClass.getFirstChild();
        PhpPsiElement scopeForUseOperator = firstChild != null ? PhpCodeInsightUtil.findScopeForUseOperator(firstChild) : null;
        if (scopeForUseOperator != null) {
            PhpElementsUtil.insertUseIfNecessary(scopeForUseOperator, DoctrineUtil.DOCTRINE_ORM_MAPPING, "ORM");
            PsiDocumentManager.getInstance(psiFile.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
        }

        PhpDocUtil.addClassEmbeddedDocs(phpClass, editor.getDocument(), psiFile);
    }
}
