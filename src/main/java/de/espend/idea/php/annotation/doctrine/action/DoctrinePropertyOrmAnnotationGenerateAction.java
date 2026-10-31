package de.espend.idea.php.annotation.doctrine.action;

import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.annotation.component.ActionRefAnchor;
import consulo.application.Application;
import consulo.codeEditor.Editor;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.action.CodeInsightAction;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.editor.hint.HintManager;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.IdeActions;
import consulo.ui.ex.popup.BaseListPopupStep;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.ui.ex.popup.ListPopup;
import consulo.ui.ex.popup.PopupStep;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.icon.PhpAnnotationIconGroup;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ActionImpl(
    id = "PhpAnnotation.Doctrine.Orm.PropertyGenerator",
    parents = @ActionParentRef(value = @ActionRef(id = IdeActions.GROUP_GENERATE), anchor = ActionRefAnchor.LAST)
)
public class DoctrinePropertyOrmAnnotationGenerateAction extends CodeInsightAction {
    private static final ElementPattern<PsiElement> INSIDE_PHP_CLASS_PATTERN = PlatformPatterns.psiElement().inside(PhpClass.class);

    public DoctrinePropertyOrmAnnotationGenerateAction() {
        super(LocalizeValue.localizeTODO("ORM Annotation"), LocalizeValue.empty(), PhpAnnotationIconGroup.doctrine());
    }

    @Override
    protected boolean isValidForFile(Project project, Editor editor, PsiFile file) {
        if (!(file instanceof PhpFile) || !DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false;
        }

        int offset = editor.getCaretModel().getOffset();
        if (offset <= 0) {
            return false;
        }

        PsiElement psiElement = file.findElementAt(offset);
        if (psiElement == null || !INSIDE_PHP_CLASS_PATTERN.accepts(psiElement)) {
            return false;
        }

        PhpClass phpClass = PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
        return phpClass != null && phpClass.getOwnFields().length > 0;
    }

    @Override
    protected CodeInsightActionHandler getHandler() {
        return new PropertyOrmHandler();
    }

    /**
     * Replacement for PhpStorm's {@code PhpGenerateFieldAccessorHandlerBase} based handler:
     * choose one of the not yet mapped fields and add the ORM metadata to it.
     */
    private static class PropertyOrmHandler implements CodeInsightActionHandler {
        @RequiredUIAccess
        @Override
        public void invoke(Project project, Editor editor, PsiFile file) {
            // TODO consulo-php: PhpGenerateFieldAccessorHandlerBase / PhpAccessorMethodData missing; local single-selection chooser instead of member chooser
            PsiElement psiElement = file.findElementAt(editor.getCaretModel().getOffset());
            PhpClass phpClass = PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
            if (phpClass == null) {
                return;
            }

            List<Field> fields = new ArrayList<>();
            for (Field field : phpClass.getOwnFields()) {
                if (!field.isConstant() && !DoctrineUtil.isOrmColumnProperty(field)) {
                    fields.add(field);
                }
            }

            if (fields.isEmpty()) {
                if (!Application.get().isHeadlessEnvironment()) {
                    HintManager.getInstance().showErrorHint(editor, "No possible orm property found");
                }
                return;
            }

            if (fields.size() == 1 || Application.get().isHeadlessEnvironment()) {
                addOrmDocs(project, editor, file, fields);
                return;
            }

            ListPopup popup = JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<>("Choose Field", fields) {
                @Override
                public String getTextFor(Field value) {
                    return value.getName();
                }

                @Override
                public @Nullable PopupStep onChosen(Field selectedValue, boolean finalChoice) {
                    return doFinalStep(() -> addOrmDocs(project, editor, file, List.of(selectedValue)));
                }
            });

            editor.showPopupInBestPositionFor(popup);
        }

        private static void addOrmDocs(Project project, Editor editor, PsiFile file, List<Field> fields) {
            WriteCommandAction.writeCommandAction(project)
                .withName("ORM Annotation")
                .run(() -> {
                    for (Field field : fields) {
                        DoctrineUtil.importOrmUseAliasIfNotExists(field);
                        PhpDocUtil.addPropertyOrmDocs(field, editor.getDocument(), file);
                    }
                });
        }

        @Override
        public boolean startInWriteAction() {
            return false;
        }
    }
}
