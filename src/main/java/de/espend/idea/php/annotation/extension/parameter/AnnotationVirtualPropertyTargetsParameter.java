package de.espend.idea.php.annotation.extension.parameter;

import consulo.language.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationVirtualPropertyTargetsParameter {
    private final PhpClass phpClass;

    private final PsiElement psiElement;

    private final String property;

    private final Collection<PsiElement> targets = new ArrayList<>();

    public AnnotationVirtualPropertyTargetsParameter(PhpClass phpClass, PsiElement psiElement, String property) {
        this.phpClass = phpClass;
        this.psiElement = psiElement;
        this.property = property;
    }

    public PhpClass getPhpClass() {
        return phpClass;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public String getProperty() {
        return property;
    }

    public void addTarget(PsiElement psiElement) {
        targets.add(psiElement);
    }

    public Collection<PsiElement> getTargets() {
        return targets;
    }
}
