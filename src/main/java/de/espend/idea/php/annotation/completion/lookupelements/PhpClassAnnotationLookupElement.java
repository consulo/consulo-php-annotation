package de.espend.idea.php.annotation.completion.lookupelements;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementPresentation;
import consulo.language.icon.IconDescriptorUpdaters;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpClassAnnotationLookupElement extends LookupElement {
    private final PhpClass myPhpClass;
    private final String myLookupString;
    private @Nullable UseAliasOption myAlias;
    private @Nullable InsertHandler<LookupElement> myInsertHandler;
    private @Nullable String myTypeText;

    public PhpClassAnnotationLookupElement(PhpClass phpClass) {
        myPhpClass = phpClass;
        myLookupString = phpClass.getName();
    }

    public PhpClassAnnotationLookupElement(PhpClass phpClass, @Nullable UseAliasOption alias, String lookupString) {
        myPhpClass = phpClass;
        myAlias = alias;
        myLookupString = lookupString;
    }

    public @Nullable UseAliasOption getAlias() {
        return myAlias;
    }

    public PhpClassAnnotationLookupElement withInsertHandler(InsertHandler<LookupElement> insertHandler) {
        myInsertHandler = insertHandler;
        return this;
    }

    public PhpClassAnnotationLookupElement withTypeText(String typeText) {
        myTypeText = typeText;
        return this;
    }

    @Override
    public String getLookupString() {
        return myLookupString;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setItemText(myLookupString);
        presentation.setTypeText(myTypeText != null ? myTypeText : myPhpClass.getPresentableFQN());
        presentation.setIcon(IconDescriptorUpdaters.getIcon(myPhpClass, 0));
        presentation.setStrikeout(myPhpClass.isDeprecated());
    }

    @Override
    public void handleInsert(InsertionContext context) {
        if (myInsertHandler != null) {
            myInsertHandler.handleInsert(context, this);
        }
    }

    @Override
    public Object getObject() {
        return myPhpClass;
    }
}
