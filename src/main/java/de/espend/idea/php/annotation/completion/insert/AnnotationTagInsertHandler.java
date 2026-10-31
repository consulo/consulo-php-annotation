package de.espend.idea.php.annotation.completion.insert;

import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.completion.insert.PhpInsertHandlerUtil;
import com.jetbrains.php.completion.insert.PhpReferenceInsertHandler;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.util.collection.ContainerUtil;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.completion.lookupelements.PhpClassAnnotationLookupElement;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationTagInsertHandler implements InsertHandler<LookupElement> {
    private static final AnnotationTagInsertHandler INSTANCE = new AnnotationTagInsertHandler();

    private AnnotationTagInsertHandler() {
    }

    public static AnnotationTagInsertHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void handleInsert(InsertionContext context, LookupElement lookupElement) {
        UseAliasOption alias = lookupElement instanceof PhpClassAnnotationLookupElement element ? element.getAlias() : null;

        // "ORM\Entity"
        if (alias != null) {
            if (!AnnotationInsertUtil.insertAliasUse(context, alias)) {
                return;
            }
        }
        else {
            // find alias in settings "\Foo\Bar as Car" for given PhpClass insertion context
            preAliasInsertion(context, lookupElement);

            // reuse jetbrains "use importer": this is private only so we need some workaround
            // to not implement your own algo for that
            PhpReferenceInsertHandler.getInstance().handleInsert(context, lookupElement);
        }

        // force "@Foo" => "@Foo(<caret>)"
        if (ApplicationSettings.getInstance().appendRoundBracket && !PhpInsertHandlerUtil.isStringAtCaret(context.getEditor(), "(")) {
            PhpInsertHandlerUtil.insertStringAtCaret(context.getEditor(), "()");
            context.getEditor().getCaretModel().moveCaretRelatively(-1, 0, false, false, true);
        }

        // "@" is not provide by lookupelements element because its remove by auto import so attach it if necessary
        PsiElement element = PsiUtilCore.getElementAtOffset(context.getFile(), context.getStartOffset());
        if (!element.getText().startsWith("@")) {
            context.getDocument().insertString(context.getStartOffset(), "@");
        }
    }

    /**
     * Insert class alias before PhpStorm tries to import a new use statement "\Foo\Bar as Car"
     */
    public static void preAliasInsertion(InsertionContext context, LookupElement lookupElement) {
        Collection<UseAliasOption> importsAliases = AnnotationUtil.getActiveImportsAliasesFromSettings();
        if (importsAliases.isEmpty()) {
            return;
        }

        if (!(lookupElement.getObject() instanceof PhpClass phpClass)) {
            return;
        }

        String fqn = StringUtils.stripStart(phpClass.getFQN(), "\\");

        UseAliasOption option = ContainerUtil.find(importsAliases, candidate ->
            candidate.getAlias() != null
                && candidate.getClassName() != null
                && fqn.startsWith(StringUtils.stripStart(candidate.getClassName(), "\\"))
        );

        if (option == null) {
            return;
        }

        String classNameSetting = option.getClassName();
        String alias = option.getAlias();
        if (classNameSetting == null || alias == null) {
            return;
        }

        PsiElement element = context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
        if (element == null) {
            return;
        }

        PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(element);
        if (scope == null) {
            return;
        }

        String className = classNameSetting.startsWith("\\") ? classNameSetting : "\\" + classNameSetting;
        PhpElementsUtil.insertUseIfNecessary(scope, className, alias);

        PsiDocumentManager.getInstance(context.getProject()).doPostponedOperationsAndUnblockDocument(context.getDocument());
    }
}
