package de.espend.idea.php.annotation.completion.insert;

import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Shared insert handler helpers
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
final class AnnotationInsertUtil {
    private AnnotationInsertUtil() {
    }

    static boolean insertAliasUse(InsertionContext context, UseAliasOption alias) {
        PsiElement element = PsiUtilCore.getElementAtOffset(context.getFile(), context.getStartOffset());
        PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(element);
        if (scope == null) {
            return false;
        }

        String className = "\\" + StringUtils.stripStart(alias.getClassName(), "\\");
        PhpElementsUtil.insertUseIfNecessary(scope, className, alias.getAlias());

        PsiDocumentManager.getInstance(context.getProject()).doPostponedOperationsAndUnblockDocument(context.getDocument());
        return true;
    }
}
