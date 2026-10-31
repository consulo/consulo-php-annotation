package de.espend.idea.php.annotation.doctrine.reference.references;

import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementResolveResult;
import consulo.language.psi.PsiPolyVariantReferenceBase;
import consulo.language.psi.ResolveResult;
import de.espend.idea.php.annotation.util.PhpElementsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineRepositoryReference extends PsiPolyVariantReferenceBase<PsiElement> {
    private final StringLiteralExpression myStringLiteral;
    private final String myContent;

    public DoctrineRepositoryReference(StringLiteralExpression psiElement) {
        super(psiElement);
        myStringLiteral = psiElement;
        myContent = psiElement.getContents();
    }

    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        PhpClass phpClass = PhpElementsUtil.getClassInsideAnnotation(myStringLiteral, myContent);
        if (phpClass == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        return new ResolveResult[]{new PsiElementResolveResult(phpClass)};
    }

    @Override
    public Object[] getVariants() {
        List<PhpClass> phpClasses = new ArrayList<>();
        PhpIndex.getInstance(getElement().getProject()).processAllSubclasses("\\Doctrine\\Common\\Persistence\\ObjectRepository", phpClass -> {
            phpClasses.add(phpClass);
            return true;
        });

        List<LookupElementBuilder> lookupElements = new ArrayList<>();
        for (PhpClass phpClass : phpClasses) {
            lookupElements.add(LookupElementBuilder.create(phpClass.getPresentableFQN()).withIcon(PhpIcons.CLASS));
        }

        return lookupElements.toArray();
    }
}
