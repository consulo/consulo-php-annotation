package de.espend.idea.php.annotation.extension;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;

import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface PhpAnnotationUseAlias {
    /**
     * Provide custom aliases for annotations eg: "ORM" => "Doctrine\\ORM\\Mapping"
     */
    Map<String, String> getAliases();
}
