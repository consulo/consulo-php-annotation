package de.espend.idea.php.annotation.completion.insert;

import com.jetbrains.php.completion.insert.PhpInsertHandlerUtil;
import com.jetbrains.php.completion.insert.PhpReferenceInsertHandler;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.completion.lookupelements.PhpClassAnnotationLookupElement;
import de.espend.idea.php.annotation.dict.UseAliasOption;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AttributeAliasInsertHandler implements InsertHandler<LookupElement> {
    private static final AttributeAliasInsertHandler INSTANCE = new AttributeAliasInsertHandler();

    private AttributeAliasInsertHandler() {
    }

    public static AttributeAliasInsertHandler getInstance() {
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
            AnnotationTagInsertHandler.preAliasInsertion(context, lookupElement);

            // reuse jetbrains "use importer": this is private only so we need some workaround
            // to not implement your own algo for that
            PhpReferenceInsertHandler.getInstance().handleInsert(context, lookupElement);
        }

        // force "#[Foo]" => "#[Foo(<caret>)]"
        if (ApplicationSettings.getInstance().appendRoundBracket && !PhpInsertHandlerUtil.isStringAtCaret(context.getEditor(), "(")) {
            PhpInsertHandlerUtil.insertStringAtCaret(context.getEditor(), "()");
            context.getEditor().getCaretModel().moveCaretRelatively(-1, 0, false, false, true);
        }
    }
}
