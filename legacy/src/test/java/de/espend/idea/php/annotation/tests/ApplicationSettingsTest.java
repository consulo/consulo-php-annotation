package de.espend.idea.php.annotation.tests;

import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.dict.UseAliasOption;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSettingsTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void testApplicationServiceCanBeCreated() {
        ApplicationSettings settings = ApplicationSettings.getInstance();

        assertSame(settings, ApplicationSettings.getInstance());
        assertSame(settings, settings.getState());
    }

    public void testLoadStateCopiesValues() {
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationSettings state = new ApplicationSettings();
        state.appendRoundBracket = false;
        List<UseAliasOption> useAliasOptions = new ArrayList<>();
        useAliasOptions.add(new UseAliasOption("App\\Annotation", "App", false));
        state.useAliasOptions = useAliasOptions;
        state.provideDefaults = false;

        settings.loadState(state);

        assertFalse(settings.appendRoundBracket);
        assertFalse(settings.provideDefaults);
        assertEquals(1, settings.useAliasOptions.size());
        assertEquals("App\\Annotation", settings.useAliasOptions.get(0).getClassName());
        assertEquals("App", settings.useAliasOptions.get(0).getAlias());
        assertFalse(settings.useAliasOptions.get(0).isEnabled());
    }

    public void testProvidesBuiltInAliases() {
        UseAliasOption option = null;
        for (UseAliasOption useAliasOption : ApplicationSettings.getDefaultUseAliasOption()) {
            if ("Doctrine\\ORM\\Mapping".equals(useAliasOption.getClassName())) {
                option = useAliasOption;
                break;
            }
        }

        assertNotNull(option);
        assertEquals("ORM", option.getAlias());
        assertTrue(option.isEnabled());
    }
}
