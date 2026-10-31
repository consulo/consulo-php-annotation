/**
 * PHP Annotations / Attributes plugin.
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
import org.jspecify.annotations.NullMarked;

@NullMarked
module de.espend.idea.php.annotation {
    requires consulo.ide.api;
    requires consulo.language.editor.ui.api;
    requires consulo.file.template.api;
    requires consulo.php.api;
    requires consulo.php;

    requires org.apache.commons.lang3;

    exports de.espend.idea.php.annotation;
    exports de.espend.idea.php.annotation.annotator;
    exports de.espend.idea.php.annotation.completion;
    exports de.espend.idea.php.annotation.completion.insert;
    exports de.espend.idea.php.annotation.completion.lookupelements;
    exports de.espend.idea.php.annotation.dict;
    exports de.espend.idea.php.annotation.doctrine.action;
    exports de.espend.idea.php.annotation.doctrine.inspection;
    exports de.espend.idea.php.annotation.doctrine.intention;
    exports de.espend.idea.php.annotation.doctrine.navigation;
    exports de.espend.idea.php.annotation.doctrine.reference;
    exports de.espend.idea.php.annotation.doctrine.reference.references;
    exports de.espend.idea.php.annotation.doctrine.util;
    exports de.espend.idea.php.annotation.extension;
    exports de.espend.idea.php.annotation.extension.parameter;
    exports de.espend.idea.php.annotation.icon;
    exports de.espend.idea.php.annotation.inspection;
    exports de.espend.idea.php.annotation.inspection.visitor;
    exports de.espend.idea.php.annotation.navigation;
    exports de.espend.idea.php.annotation.pattern;
    exports de.espend.idea.php.annotation.reference;
    exports de.espend.idea.php.annotation.reference.references;
    exports de.espend.idea.php.annotation.symfony;
    exports de.espend.idea.php.annotation.ui;
    exports de.espend.idea.php.annotation.util;

    opens de.espend.idea.php.annotation to consulo.util.xml.serializer;
    opens de.espend.idea.php.annotation.dict to consulo.util.xml.serializer;
}
