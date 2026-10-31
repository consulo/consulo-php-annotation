package de.espend.idea.php.annotation.reference;

import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiReference;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import de.espend.idea.php.annotation.reference.references.PhpClassReference;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationClassProvider implements PhpAnnotationReferenceProvider {
    @Override
    public PsiReference @Nullable [] getPropertyReferences(
        AnnotationPropertyParameter annotationPropertyParameter,
        PhpAnnotationReferenceProviderParameter referencesByElementParameter
    ) {
        if (annotationPropertyParameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return null;
        }

        if (!"targetEntity".equals(annotationPropertyParameter.getPropertyName())) {
            return null;
        }

        if (!(annotationPropertyParameter.getElement() instanceof StringLiteralExpression stringLiteralExpression)) {
            return null;
        }

        return new PsiReference[]{new PhpClassReference(stringLiteralExpression)};
    }
}
