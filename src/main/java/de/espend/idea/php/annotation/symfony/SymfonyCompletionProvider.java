package de.espend.idea.php.annotation.symfony;

import com.jetbrains.php.lang.PhpLangUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class SymfonyCompletionProvider implements PhpAnnotationCompletionProvider {
    private static final String[] METHODS = {"HEAD", "GET", "POST", "PUT", "PATCH", "DELETE", "PURGE", "OPTIONS", "TRACE", "CONNECT"};

    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter parameter, AnnotationCompletionProviderParameter completion) {
        if (parameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_ARRAY) {
            return;
        }

        String fqn = StringUtils.stripStart(parameter.getPhpClass().getFQN(), "\\");
        boolean supportsMethods = "methods".equals(parameter.getPropertyName())
            && (PhpLangUtil.equalsClassNames(fqn, "Symfony\\Component\\Routing\\Annotation\\Route")
            || PhpLangUtil.equalsClassNames(fqn, "Symfony\\Component\\Routing\\Attribute\\Route"));

        if (supportsMethods) {
            for (String method : METHODS) {
                completion.getResult().addElement(LookupElementBuilder.create(method));
            }
        }
    }
}
