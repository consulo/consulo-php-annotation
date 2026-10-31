package de.espend.idea.php.annotation.tests.ui;

import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import de.espend.idea.php.annotation.ui.SettingsForm;

public class SettingsFormTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void testCanBeCreated() {
        SettingsForm configurable = new SettingsForm();

        assertNotNull(configurable.createComponent());
        assertFalse(configurable.isModified());
    }
}
