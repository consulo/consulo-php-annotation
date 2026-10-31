package de.espend.idea.php.annotation.extension.parameter;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.dict.AnnotationPropertyEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationVirtualPropertyCompletionParameter {
    private final PhpClass phpClass;

    final private Map<String, AnnotationPropertyEnum> lookupElements = new HashMap<>();

    public AnnotationVirtualPropertyCompletionParameter(PhpClass phpClass) {
        this.phpClass = phpClass;
    }

    public void addLookupElement(String element, AnnotationPropertyEnum propertyEnum) {
        lookupElements.put(element, propertyEnum);
    }

    public Map<String, AnnotationPropertyEnum> getLookupElements() {
        return lookupElements;
    }

    public PhpClass getPhpClass() {
        return phpClass;
    }
}
