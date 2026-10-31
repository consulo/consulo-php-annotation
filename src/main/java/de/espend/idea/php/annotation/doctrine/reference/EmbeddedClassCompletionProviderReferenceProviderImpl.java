package de.espend.idea.php.annotation.doctrine.reference;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiReference;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import org.jspecify.annotations.Nullable;

/**
 * {@link PhpAnnotationReferenceProvider} registration of {@link EmbeddedClassCompletionProvider}
 */
@ExtensionImpl
public class EmbeddedClassCompletionProviderReferenceProviderImpl implements PhpAnnotationReferenceProvider {
    private final EmbeddedClassCompletionProvider myDelegate = new EmbeddedClassCompletionProvider();

    @Override
    public PsiReference @Nullable [] getPropertyReferences(
        AnnotationPropertyParameter annotationPropertyParameter,
        PhpAnnotationReferenceProviderParameter referencesByElementParameter
    ) {
        return myDelegate.getPropertyReferences(annotationPropertyParameter, referencesByElementParameter);
    }
}
