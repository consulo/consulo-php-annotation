package de.espend.idea.php.annotation.completion.lookupelements;

import com.jetbrains.php.PhpIcons;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementPresentation;
import de.espend.idea.php.annotation.completion.insert.AnnotationPropertyInsertHandler;
import de.espend.idea.php.annotation.dict.AnnotationProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpAnnotationPropertyLookupElement extends LookupElement {
    private final AnnotationProperty myAnnotationProperty;

    public PhpAnnotationPropertyLookupElement(AnnotationProperty annotationProperty) {
        myAnnotationProperty = annotationProperty;
    }

    @Override
    public String getLookupString() {
        return myAnnotationProperty.getPropertyName();
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setItemText(getLookupString());
        presentation.setTypeText(StringUtils.capitalize(myAnnotationProperty.getAnnotationPropertyEnum().name().toLowerCase(Locale.ROOT)));
        presentation.setTypeGrayed(true);
        presentation.setIcon(PhpIcons.FIELD);
    }

    @Override
    public void handleInsert(InsertionContext context) {
        AnnotationPropertyInsertHandler.getInstance().handleInsert(context, this);
    }

    @Override
    public Object getObject() {
        return myAnnotationProperty;
    }
}
