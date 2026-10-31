package de.espend.idea.php.annotation.doctrine.reference;

import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementResolveResult;
import consulo.language.psi.PsiPolyVariantReferenceBase;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ResolveResult;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class DoctrineAnnotationFieldTypeProvider implements PhpAnnotationReferenceProvider {
    @Override
    public PsiReference @Nullable [] getPropertyReferences(
        AnnotationPropertyParameter annotationPropertyParameter,
        PhpAnnotationReferenceProviderParameter referencesByElementParameter
    ) {
        if (annotationPropertyParameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return null;
        }

        if (!"type".equals(annotationPropertyParameter.getPropertyName())) {
            return null;
        }

        String presentableFQN = annotationPropertyParameter.getPhpClass().getPresentableFQN();
        if (!presentableFQN.startsWith("\\")) {
            presentableFQN = "\\" + presentableFQN;
        }

        if (!"\\Doctrine\\ORM\\Mapping\\Column".equals(presentableFQN)) {
            return null;
        }

        if (!(annotationPropertyParameter.getElement() instanceof StringLiteralExpression element)) {
            return null;
        }

        return new PsiReference[]{new DoctrineColumnTypeReference(element)};
    }

    private static class DoctrineColumnTypeReference extends PsiPolyVariantReferenceBase<PsiElement> {
        private final StringLiteralExpression myStringLiteral;

        DoctrineColumnTypeReference(StringLiteralExpression psiElement) {
            super(psiElement);
            myStringLiteral = psiElement;
        }

        @Override
        public ResolveResult[] multiResolve(boolean incompleteCode) {
            String contents = myStringLiteral.getContents();
            if (StringUtils.isBlank(contents)) {
                return ResolveResult.EMPTY_ARRAY;
            }

            return PsiElementResolveResult.createResults(DoctrineUtil.getColumnTypesTargets(myStringLiteral.getProject(), contents));
        }

        @Override
        public Object[] getVariants() {
            return DoctrineUtil.getTypes(myStringLiteral.getProject()).toArray();
        }
    }
}
