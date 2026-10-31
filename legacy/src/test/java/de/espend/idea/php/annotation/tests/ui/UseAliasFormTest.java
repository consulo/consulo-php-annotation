package de.espend.idea.php.annotation.tests.ui;

import com.intellij.openapi.ui.DialogWrapper;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import de.espend.idea.php.annotation.ui.UseAliasForm;

public class UseAliasFormTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void testCanBeCreated() {
        UseAliasForm form = new UseAliasForm(null, new UseAliasOption("Doctrine\\ORM\\Mapping", "ORM", true), option -> {
        });

        assertNotNull(form.getPreferredFocusedComponent());
        form.close(DialogWrapper.CANCEL_EXIT_CODE);
    }
}
