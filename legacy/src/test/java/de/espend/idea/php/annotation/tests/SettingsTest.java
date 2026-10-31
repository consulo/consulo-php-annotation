package de.espend.idea.php.annotation.tests;

import de.espend.idea.php.annotation.Settings;

public class SettingsTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void testProjectServiceCanBeCreated() {
        Settings settings = Settings.getInstance(getProject());

        assertSame(settings, Settings.getInstance(getProject()));
        assertSame(settings, settings.getState());
    }
}
