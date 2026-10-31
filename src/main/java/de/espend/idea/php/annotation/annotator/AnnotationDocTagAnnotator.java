package de.espend.idea.php.annotation.annotator;

import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import consulo.language.psi.PsiElement;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagAnnotator;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationDocTagAnnotatorParameter;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationDocTagAnnotator implements Annotator, AnnotatorFactory {
    @Override
    public @Nullable Annotator createAnnotator() {
        return this;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    @Override
    public void annotate(PsiElement psiElement, AnnotationHolder holder) {
        if (!(psiElement instanceof PhpDocTag phpDocTag)) {
            return;
        }

        String name = phpDocTag.getName();
        if (AnnotationUtil.isBlockedAnnotationTag(name)) {
            return;
        }

        if (!AnnotationUtil.isAnnotationPhpDocTag(phpDocTag)) {
            return;
        }

        PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
        if (phpClass == null) {
            PhpAnnotationDocTagAnnotatorParameter parameter = new PhpAnnotationDocTagAnnotatorParameter(phpDocTag, holder);
            for (PhpAnnotationDocTagAnnotator annotator : AnnotationUtil.EP_DOC_TAG_ANNOTATOR.getExtensionList()) {
                annotator.annotate(parameter);
            }
            return;
        }

        PhpAnnotationDocTagAnnotatorParameter parameter = new PhpAnnotationDocTagAnnotatorParameter(phpClass, phpDocTag, holder);
        for (PhpAnnotationDocTagAnnotator annotator : AnnotationUtil.EP_DOC_TAG_ANNOTATOR.getExtensionList()) {
            annotator.annotate(parameter);
        }
    }
}
