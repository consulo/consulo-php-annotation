package de.espend.idea.php.annotation.extension.parameter;

import consulo.language.editor.annotation.AnnotationHolder;
import consulo.project.Project;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpAnnotationDocTagAnnotatorParameter {

    private final PhpDocTag phpDocTag;
    private final AnnotationHolder holder;

    @Nullable
    private PhpClass annotationClass;

    public PhpAnnotationDocTagAnnotatorParameter(PhpDocTag phpDocTag, AnnotationHolder holder) {
        this.phpDocTag = phpDocTag;
        this.holder = holder;
    }

    public PhpAnnotationDocTagAnnotatorParameter(@Nullable PhpClass annotationClass, PhpDocTag phpDocTag, AnnotationHolder holder) {
        this(phpDocTag, holder);
        this.annotationClass = annotationClass;
    }

    public PhpDocTag getPhpDocTag() {
        return phpDocTag;
    }

    @Nullable
    public PhpDocTagAnnotation getAnnotationDocTag() {
        return AnnotationUtil.getPhpDocAnnotationContainer(this.phpDocTag);
    }

    public AnnotationHolder getHolder() {
        return holder;
    }

    public Project getProject() {
        return this.phpDocTag.getProject();
    }

    @Nullable
    public PhpClass getAnnotationClass() {
        return annotationClass;
    }

}
