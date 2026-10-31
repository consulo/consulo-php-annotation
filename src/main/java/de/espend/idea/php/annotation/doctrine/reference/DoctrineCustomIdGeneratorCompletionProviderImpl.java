package de.espend.idea.php.annotation.doctrine.reference;

import consulo.annotation.component.ExtensionImpl;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;

/**
 * {@link PhpAnnotationCompletionProvider} registration of {@link DoctrineCustomIdGenerator}
 */
@ExtensionImpl
public class DoctrineCustomIdGeneratorCompletionProviderImpl implements PhpAnnotationCompletionProvider {
    private final DoctrineCustomIdGenerator myDelegate = new DoctrineCustomIdGenerator();

    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, AnnotationCompletionProviderParameter completionParameter) {
        myDelegate.getPropertyValueCompletions(annotationPropertyParameter, completionParameter);
    }
}
