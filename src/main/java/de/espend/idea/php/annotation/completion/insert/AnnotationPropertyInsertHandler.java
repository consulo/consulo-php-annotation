package de.espend.idea.php.annotation.completion.insert;

import com.jetbrains.php.completion.insert.PhpInsertHandlerUtil;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import de.espend.idea.php.annotation.dict.AnnotationProperty;
import de.espend.idea.php.annotation.dict.AnnotationPropertyEnum;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationPropertyInsertHandler implements InsertHandler<LookupElement> {
    private static final AnnotationPropertyInsertHandler INSTANCE = new AnnotationPropertyInsertHandler();

    private AnnotationPropertyInsertHandler() {
    }

    public static AnnotationPropertyInsertHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void handleInsert(InsertionContext context, LookupElement lookupElement) {
        // value completion should not fire when already presented:
        // eng| = "value"
        // eng|="value"
        if (PhpInsertHandlerUtil.isStringAtCaret(context.getEditor(), "=")
            || PhpInsertHandlerUtil.isStringAtCaret(context.getEditor(), " =")) {
            return;
        }

        AnnotationPropertyEnum propertyEnum = null;
        if (lookupElement.getObject() instanceof AnnotationProperty property) {
            propertyEnum = property.getAnnotationPropertyEnum();
        }

        // append completion text depend on value:
        // engine="|"
        // engine={|}
        // engine=<boolean|integer>
        String textToInsert;
        if (propertyEnum == AnnotationPropertyEnum.ARRAY) {
            textToInsert = "={}";
        }
        else if (isNumberOrBoolean(propertyEnum)) {
            textToInsert = "=";
        }
        else {
            textToInsert = "=\"\"";
        }

        PhpInsertHandlerUtil.insertStringAtCaret(context.getEditor(), textToInsert);

        // move caret back
        if (!isNumberOrBoolean(propertyEnum)) {
            context.getEditor().getCaretModel().moveCaretRelatively(-1, 0, false, false, true);
        }
    }

    private static boolean isNumberOrBoolean(@Nullable AnnotationPropertyEnum propertyEnum) {
        return propertyEnum == AnnotationPropertyEnum.INTEGER || propertyEnum == AnnotationPropertyEnum.BOOLEAN;
    }
}
