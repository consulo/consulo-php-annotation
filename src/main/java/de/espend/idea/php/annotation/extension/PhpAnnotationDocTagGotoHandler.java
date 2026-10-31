package de.espend.idea.php.annotation.extension;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import de.espend.idea.php.annotation.extension.parameter.AnnotationDocTagGotoHandlerParameter;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface PhpAnnotationDocTagGotoHandler {
    void getGotoDeclarationTargets(AnnotationDocTagGotoHandlerParameter parameter);
}
