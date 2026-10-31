package de.espend.idea.php.annotation.doctrine.reference;

import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import de.espend.idea.php.annotation.doctrine.reference.references.DoctrinePhpClassFieldReference;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import de.espend.idea.php.annotation.util.PhpPsiAttributesUtil;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class DoctrineAnnotationFieldProvider implements PhpAnnotationReferenceProvider {
    @Override
    public PsiReference @Nullable [] getPropertyReferences(
        AnnotationPropertyParameter annotationPropertyParameter,
        PhpAnnotationReferenceProviderParameter referencesByElementParameter
    ) {
        if (annotationPropertyParameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return null;
        }

        String propertyName = annotationPropertyParameter.getPropertyName();
        if (!"mappedBy".equals(propertyName) && !"inversedBy".equals(propertyName)) {
            return null;
        }

        if (!(annotationPropertyParameter.getElement() instanceof StringLiteralExpression element)) {
            return null;
        }

        PsiElement parent = element.getParent();
        if (parent instanceof ParameterList) {
            if (parent.getParent() instanceof PhpAttribute phpAttribute) {
                String targetEntityValue = PhpPsiAttributesUtil.getAttributeValueByNameAsStringWithClassConstant(phpAttribute, "targetEntity");
                if (targetEntityValue != null) {
                    PhpClass phpClass = PhpElementsUtil.getClassInterface(annotationPropertyParameter.getProject(), targetEntityValue);
                    if (phpClass == null) {
                        return null;
                    }

                    return new PsiReference[]{new DoctrinePhpClassFieldReference(element, phpClass)};
                }
            }
        }
        else {
            StringLiteralExpression targetEntity = PhpElementsUtil.getChildrenOnPatternMatch(
                parent,
                AnnotationPattern.getPropertyIdentifierValue("targetEntity")
            );

            if (targetEntity == null) {
                return null;
            }

            PhpClass phpClass = PhpElementsUtil.getClassInsideAnnotation(targetEntity);
            if (phpClass == null) {
                return null;
            }

            return new PsiReference[]{new DoctrinePhpClassFieldReference(element, phpClass)};
        }

        return null;
    }
}
