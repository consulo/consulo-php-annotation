package de.espend.idea.php.annotation.inspection;

import consulo.language.editor.hint.HintManager;
import consulo.language.editor.inspection.LocalQuickFixAndIntentionActionOnPsiElement;
import consulo.language.editor.WriteCommandAction;
import consulo.codeEditor.Editor;
import consulo.project.Project;
import consulo.ui.ex.popup.JBPopup;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.localize.LocalizeValue;
import consulo.util.lang.Pair;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.refactoring.PhpAliasImporter;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ImportUseForAnnotationQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
    private final Collection<Pair<String, @Nullable String>> myClasses;

    ImportUseForAnnotationQuickFix(PhpDocTag phpDocTag, Collection<Pair<String, @Nullable String>> classes) {
        super(phpDocTag);
        myClasses = classes;
    }

    @Override
    public LocalizeValue getText() {
        return LocalizeValue.of(getTextString());
    }

    private String getTextString() {
        // single class presentable item
        if(myClasses.size() == 1) {
            Pair<String, @Nullable String> next = myClasses.iterator().next();
            String message = "Import: " + StringUtils.stripStart(next.getFirst(), "\\");

            String alias = next.getSecond();
            if (alias != null) {
                message += " -> " + alias;
            }

            return message;
        }

        // multiple classes found just display overall count
        return String.format("Import class (%s)", myClasses.size());
    }

    @Override
    public void invoke(Project project, PsiFile psiFile, @Nullable Editor editor, PsiElement startElement, PsiElement endElement) {
        if(editor == null) {
            return;
        }

        if(myClasses.isEmpty()) {
            HintManager.getInstance().showErrorHint(editor, "Ops, nothing found");
            return;
        }

        // single item, directly run it
        if(myClasses.size() == 1) {
            invoke(startElement, myClasses.iterator().next());
            return;
        }

        // strip first "\"
        List<PopupChooserItem> popupChooserItems = myClasses.stream()
            .map(pair -> {
                PhpClass classInterface = PhpElementsUtil.getClassInterface(project, pair.getFirst());
                return new PopupChooserItem(pair, classInterface != null && classInterface.isDeprecated());
            })
            .collect(Collectors.toList());

        JBPopup popup = JBPopupFactory.getInstance().createPopupChooserBuilder(popupChooserItems)
            .setTitle("Import: Annotation Suggestion")
            .setItemChosenCallback(selected ->
                WriteCommandAction.writeCommandAction(editor.getProject())
                    .withName("Import: " + selected)
                    .run(() -> invoke(startElement, selected.item()))
            )
            .createPopup();

        editor.showPopupInBestPositionFor(popup);
    }

    private void invoke(PsiElement psiElement, Pair<String, @Nullable String> pair) {
        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(psiElement);
        if(scopeForUseOperator != null) {
            if (pair.getSecond() == null) {
                PhpAliasImporter.insertUseStatement(pair.getFirst(), scopeForUseOperator);
            } else {
                PhpAliasImporter.insertUseStatement(pair.getFirst(), pair.getSecond(), scopeForUseOperator);
            }
        }
    }

    private record PopupChooserItem(Pair<String, @Nullable String> item, boolean isDeprecated) {
        @Override
        public String toString() {
            String itemText = StringUtils.stripStart(item.getFirst(), "\\");

            String alias = item.getSecond();
            if (alias != null) {
                itemText += " -> " + alias;
            }

            if (isDeprecated) {
                itemText += " (Deprecated)";
            }

            return itemText;
        }
    }
}
