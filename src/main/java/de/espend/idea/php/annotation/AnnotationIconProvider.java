package de.espend.idea.php.annotation;

import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.icon.IconDescriptor;
import consulo.language.icon.IconDescriptorUpdater;
import consulo.language.psi.PsiElement;
import consulo.platform.base.icon.PlatformIconGroup;
import de.espend.idea.php.annotation.util.AnnotationUtil;

/**
 * Change file icon for a file with only one class and annotate with "@Annotation"
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationIconProvider implements IconDescriptorUpdater {
    @RequiredReadAction
    @Override
    public void updateIcon(IconDescriptor iconDescriptor, PsiElement element, int flags) {
        if (element instanceof PhpFile phpFile && PhpPsiUtil.findClasses(phpFile, AnnotationUtil::isAnnotationClass).size() == 1) {
            iconDescriptor.setMainIcon(PlatformIconGroup.nodesAnnotationtype());
        }
    }
}
