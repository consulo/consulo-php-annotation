package de.espend.idea.php.annotation.reference.references;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementResolveResult;
import consulo.language.psi.PsiPolyVariantReferenceBase;
import consulo.language.psi.ResolveResult;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.util.PhpElementsUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpClassReference extends PsiPolyVariantReferenceBase<PsiElement> {

    final private String content;

    public PhpClassReference(StringLiteralExpression psiElement) {
        super(psiElement);
        this.content = psiElement.getContents();
    }

    @Override
    public ResolveResult [] multiResolve(boolean b) {

        PhpClass phpClass = PhpElementsUtil.getClassInsideAnnotation((StringLiteralExpression) getElement(), content);
        if(phpClass == null) {
            return new ResolveResult[0];
        }

        return new ResolveResult[] {
            new PsiElementResolveResult(phpClass)
        };
    }

    @Override
    public Object [] getVariants() {
        return new Object[0];
    }
}