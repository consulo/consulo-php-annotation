package de.espend.idea.php.annotation.doctrine.navigation;

import com.jetbrains.php.lang.PhpLangUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class DoctrineAnnotationStaticCompletionProvider implements PhpAnnotationCompletionProvider {
    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, AnnotationCompletionProviderParameter completionParameter) {
        if (annotationPropertyParameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return;
        }

        if ("onDelete".equals(annotationPropertyParameter.getPropertyName())
            && PhpLangUtil.equalsClassNames(annotationPropertyParameter.getPhpClass().getPresentableFQN(), "Doctrine\\ORM\\Mapping\\JoinColumn")) {
            for (String value : new String[]{"CASCADE", "SET NULL"}) {
                completionParameter.getResult().addElement(LookupElementBuilder.create(value));
            }
        }
    }
}
