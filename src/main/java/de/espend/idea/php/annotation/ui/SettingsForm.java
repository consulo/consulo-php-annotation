package de.espend.idea.php.annotation.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ApplicationConfigurable;
import consulo.configurable.SimpleConfigurableByProperties;
import consulo.configurable.StandardConfigurableIds;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.ui.Button;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.LabeledLayout;
import consulo.ui.layout.VerticalLayout;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.util.PluginUtil;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class SettingsForm extends SimpleConfigurableByProperties implements ApplicationConfigurable {
    public static final String ID = "PhpAnnotations.SettingsForm";

    private final ApplicationSettings mySettings;

    @Inject
    public SettingsForm(ApplicationSettings settings) {
        mySettings = settings;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public @Nullable String getParentId() {
        // upstream parent "reference.webide.settings.project.settings.php" has no Consulo counterpart
        return StandardConfigurableIds.EDITOR_GROUP;
    }

    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("PHP Annotations / Attributes");
    }

    @RequiredUIAccess
    @Override
    protected Component createLayout(PropertyBuilder propertyBuilder, Disposable uiDisposable) {
        Button buttonCleanIndex = Button.create(
            LocalizeValue.localizeTODO("Schedule annotation reindex"),
            event -> PluginUtil.forceReindex()
        );

        CheckBox appendRoundBracket = CheckBox.create(LocalizeValue.localizeTODO("Insert round bracket after class name"));
        propertyBuilder.add(appendRoundBracket, () -> mySettings.appendRoundBracket, value -> mySettings.appendRoundBracket = value);

        VerticalLayout root = VerticalLayout.create();
        root.add(LabeledLayout.create(LocalizeValue.localizeTODO("Actions"), VerticalLayout.create().add(buttonCleanIndex)));
        root.add(LabeledLayout.create(LocalizeValue.localizeTODO("Autocomplete (Annotations)"), VerticalLayout.create().add(appendRoundBracket)));
        return root;
    }
}
