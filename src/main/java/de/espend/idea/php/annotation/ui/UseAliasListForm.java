package de.espend.idea.php.annotation.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ApplicationConfigurable;
import consulo.configurable.Configurable;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.ui.Button;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.ComponentItemRenderer;
import consulo.ui.Label;
import consulo.ui.MessageBoxes;
import consulo.ui.Table;
import consulo.ui.TableItemEditor;
import consulo.ui.ValueComponent;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.toolbar.AddAction;
import consulo.ui.ex.toolbar.DownMoveAction;
import consulo.ui.ex.toolbar.EditAction;
import consulo.ui.ex.toolbar.RemoveAction;
import consulo.ui.ex.toolbar.ToolbarDecoratorBuilderFactory;
import consulo.ui.ex.toolbar.UpMoveAction;
import consulo.ui.layout.DockLayout;
import consulo.ui.model.FlatDataModel;
import consulo.ui.model.MutableFlatDataModel;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class UseAliasListForm implements ApplicationConfigurable, Configurable.NoScroll {
    private final ApplicationSettings mySettings;

    private boolean myChanged;
    private @Nullable MutableFlatDataModel<UseAliasOption> myModel;
    private @Nullable Component myPanel;

    @Inject
    public UseAliasListForm(ApplicationSettings settings) {
        mySettings = settings;
    }

    @Override
    public String getId() {
        return "PhpAnnotations.UseAliasListForm";
    }

    @Override
    public @Nullable String getParentId() {
        return SettingsForm.ID;
    }

    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Use Alias");
    }

    @RequiredUIAccess
    @Override
    public @Nullable Component createUIComponent(Disposable parentDisposable) {
        MutableFlatDataModel<UseAliasOption> model = FlatDataModel.of(List.of());
        myModel = model;

        Table<UseAliasOption> table = Table.create(model);
        table.addColumn(LocalizeValue.localizeTODO("Class"), option -> StringUtils.defaultString(option.getClassName()));
        table.addColumn(LocalizeValue.localizeTODO("Alias"), option -> StringUtils.defaultString(option.getAlias()));
        table.addColumn(LocalizeValue.localizeTODO("Status"), UseAliasOption::isEnabled)
            .setWidth(50)
            .setResizable(false)
            .setRenderer(ComponentItemRenderer.reusable(
                () -> CheckBox.create(LocalizeValue.empty()),
                (checkBox, item) -> checkBox.setValue(Boolean.TRUE.equals(item.getValue()), false)
            ))
            .setEditor(new TableItemEditor<>() {
                @RequiredUIAccess
                @Override
                public ValueComponent<Boolean> createComponent(UseAliasOption option) {
                    return CheckBox.create(LocalizeValue.empty(), option.isEnabled());
                }

                @RequiredUIAccess
                @Override
                public void commit(UseAliasOption option, @Nullable Boolean value) {
                    option.setEnabled(Boolean.TRUE.equals(value));
                    model.update(option);
                    myChanged = true;
                }
            });

        Component tablePanel = ToolbarDecoratorBuilderFactory.getInstance()
            .create(table)
            .addOrReplaceAction(new AddAction<>() {
                @RequiredUIAccess
                @Override
                protected void doAdd(AnActionEvent e) {
                    UseAliasForm.create(myPanel, option -> {
                        model.add(option);
                        myChanged = true;
                    });
                }
            })
            .addOrReplaceAction(new EditAction<>() {
                @RequiredUIAccess
                @Override
                protected void doEdit(UseAliasOption value, AnActionEvent e) {
                    UseAliasForm.create(myPanel, value, option -> {
                        model.update(option);
                        myChanged = true;
                    });
                }
            })
            .addOrReplaceAction(new RemoveAction<>() {
                @RequiredUIAccess
                @Override
                protected void doRemove(UseAliasOption value, AnActionEvent e) {
                    model.remove(value);
                    myChanged = true;
                }
            })
            .disableAction(UpMoveAction.class)
            .disableAction(DownMoveAction.class)
            .build();

        Button buttonReset = Button.create(LocalizeValue.localizeTODO("Default reset"));

        DockLayout header = DockLayout.create();
        header.left(Label.create(LocalizeValue.localizeTODO("Auto insert use alias for given class scope eg \"Doctrine\\ORM\\Mapping as ORM\"")));
        header.right(buttonReset);

        DockLayout root = DockLayout.create();
        root.top(header);
        root.center(tablePanel);
        myPanel = root;

        buttonReset.addClickListener(event -> {
            resetList();
            myChanged = true;
            apply();
            mySettings.provideDefaults = false;
            MessageBoxes.okInfo(LocalizeValue.localizeTODO("Default alias applied")).showAsync(root);
        });

        return root;
    }

    @RequiredUIAccess
    private void resetList() {
        if (myModel != null) {
            myModel.replaceAll(ApplicationSettings.getDefaultUseAliasOption());
        }
    }

    @RequiredUIAccess
    @Override
    public boolean isModified() {
        return myChanged;
    }

    @RequiredUIAccess
    @Override
    public void apply() {
        if (myModel == null) {
            return;
        }

        List<UseAliasOption> options = new ArrayList<>();
        for (UseAliasOption option : myModel) {
            options.add(option);
        }

        mySettings.useAliasOptions = options;
        mySettings.provideDefaults = false;
        myChanged = false;
    }

    @RequiredUIAccess
    @Override
    public void reset() {
        if (myModel != null) {
            myModel.replaceAll(ApplicationSettings.getUseAliasOptionsWithDefaultFallback());
        }
        myChanged = false;
    }

    @RequiredUIAccess
    @Override
    public void disposeUIResources() {
        myPanel = null;
        myModel = null;
    }
}
