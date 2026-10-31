package de.espend.idea.php.annotation.extension;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationVirtualPropertyCompletionParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationVirtualPropertyTargetsParameter;

/**
 * Adds virtual fields for given class @Foo(foo='')
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface PhpAnnotationVirtualProperties {

    /**
     * Add completion elements
     */
    void addCompletions(AnnotationVirtualPropertyCompletionParameter virtualPropertyParameter, AnnotationCompletionProviderParameter parameter);

    /**
     * Add targets for given element
     */
    void getTargets(AnnotationVirtualPropertyTargetsParameter parameter);
}
