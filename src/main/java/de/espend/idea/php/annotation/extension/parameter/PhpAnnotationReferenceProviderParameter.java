package de.espend.idea.php.annotation.extension.parameter;

import consulo.language.psi.PsiElement;
import consulo.language.util.ProcessingContext;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpAnnotationReferenceProviderParameter {

    final private PsiElement psiElement;
    final private ProcessingContext processingContext;

    public PhpAnnotationReferenceProviderParameter(PsiElement psiElement, ProcessingContext processingContext) {
        this.psiElement = psiElement;
        this.processingContext = processingContext;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public ProcessingContext getProcessingContext() {
        return processingContext;
    }
}
