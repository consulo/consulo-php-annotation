package de.espend.idea.php.annotation.extension.parameter;

import consulo.project.Project;
import consulo.language.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationDocTagGotoHandlerParameter {

    final private PhpDocTag phpDocTag;
    final private List<PsiElement> targets;
    final private PhpClass phpClass;

    public AnnotationDocTagGotoHandlerParameter(PhpDocTag phpDocTag, PhpClass phpClass, List<PsiElement> targets) {
        this.phpDocTag = phpDocTag;
        this.targets = targets;
        this.phpClass = phpClass;
    }

    public Project getProject() {
        return phpDocTag.getProject();
    }

    public void addTarget(PsiElement psiElement) {
        this.targets.add(psiElement);
    }

    public void addTargets(Collection<PsiElement> psiElements) {
        this.targets.addAll(psiElements);
    }

    public List<PsiElement> getTargets() {
        return targets;
    }

    public PhpClass getPhpClass() {
        return phpClass;
    }

    public PhpDocTag getPhpDocTag() {
        return phpDocTag;
    }

    @Nullable
    public PhpDocTagAnnotation getAnnotationDocTag() {
        return AnnotationUtil.getPhpDocAnnotationContainer(this.phpDocTag);
    }

}
