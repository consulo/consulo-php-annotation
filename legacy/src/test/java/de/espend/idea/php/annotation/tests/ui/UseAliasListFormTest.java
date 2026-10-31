package de.espend.idea.php.annotation.tests.ui;

import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import de.espend.idea.php.annotation.ui.UseAliasListForm;

public class UseAliasListFormTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void testCanBeCreated() {
        UseAliasListForm configurable = new UseAliasListForm();

        assertNotNull(configurable.createComponent());
        assertFalse(configurable.isModified());
    }
}
