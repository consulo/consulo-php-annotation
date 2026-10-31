package de.espend.idea.php.annotation.ui;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.refactoring.PhpNameUtil;
import consulo.application.Application;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.TextBox;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.dialog.DialogDescriptor;
import consulo.ui.ex.dialog.DialogService;
import consulo.ui.layout.DockLayout;
import consulo.ui.style.ComponentColors;
import consulo.ui.util.FormBuilder;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class UseAliasForm extends DialogDescriptor {
    private final UseAliasOption myUseAliasOption;

    private final TextBox myTextClassName = TextBox.create();
    private final TextBox myTextAlias = TextBox.create();
    private final CheckBox myCheckStatus = CheckBox.create(LocalizeValue.localizeTODO("Enabled"));

    private LocalizeValue myErrorText = LocalizeValue.empty();
    private @Nullable Label myErrorLabel;

    @RequiredUIAccess
    public UseAliasForm(UseAliasOption useAliasOption) {
        super(LocalizeValue.localizeTODO("Use Alias"));
        myUseAliasOption = useAliasOption;

        myTextClassName.setValue(useAliasOption.getClassName());
        myTextAlias.setValue(useAliasOption.getAlias());
        myCheckStatus.setValue(useAliasOption.isEnabled());
    }

    @RequiredUIAccess
    @Override
    public Component createCenterComponent(Disposable uiDisposable) {
        myTextClassName.addValueListener(event -> validate());
        myTextAlias.addValueListener(event -> validate());

        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(LocalizeValue.localizeTODO("Class (Doctrine\\ORM\\Mapping)"), myTextClassName);
        builder.addLabeled(LocalizeValue.localizeTODO("Alias (ORM)"), myTextAlias);
        builder.addBottom(myCheckStatus);

        Label errorLabel = Label.create(LocalizeValue.empty());
        errorLabel.setForegroundColor(ComponentColors.ERROR_FOREGROUND);
        myErrorLabel = errorLabel;

        DockLayout layout = DockLayout.create();
        layout.center(builder.build());
        layout.bottom(errorLabel);

        validate();
        return layout;
    }

    @RequiredUIAccess
    @Override
    public Component getPreferredFocusedComponent() {
        return myTextClassName;
    }

    @RequiredUIAccess
    private void validate() {
        LocalizeValue errorText = LocalizeValue.empty();
        if (!PhpNameUtil.isValidNamespaceFullName(getNormalizedClassName(), PhpLanguageLevel.DEFAULT)) {
            errorText = LocalizeValue.localizeTODO("Invalid class name");
        }
        else if (!PhpNameUtil.isValidNamespaceFullName(StringUtils.defaultString(myTextAlias.getValue()), PhpLanguageLevel.DEFAULT)) {
            errorText = LocalizeValue.localizeTODO("Invalid alias");
        }

        myErrorText = errorText;
        Label errorLabel = myErrorLabel;
        if (errorLabel != null) {
            errorLabel.setText(errorText);
            errorLabel.setVisible(!errorText.isEmpty());
        }
        updateOkButtonState();
    }

    @Override
    public boolean doUpdateOkButtonState() {
        return myErrorText.isEmpty();
    }

    @RequiredUIAccess
    private void applyTo(UseAliasOption option) {
        option.setClassName(getNormalizedClassName());
        option.setAlias(StringUtils.defaultString(myTextAlias.getValue()));
        option.setEnabled(myCheckStatus.getValueOrError());
    }

    private String getNormalizedClassName() {
        return StringUtils.strip(StringUtils.defaultString(myTextClassName.getValue()), "\\");
    }

    @FunctionalInterface
    public interface Callback {
        @RequiredUIAccess
        void ok(UseAliasOption option);
    }

    @RequiredUIAccess
    public static void create(@Nullable Component component, Callback callback) {
        create(component, new UseAliasOption("", "", true), callback);
    }

    @RequiredUIAccess
    public static void create(@Nullable Component component, UseAliasOption option, Callback callback) {
        UseAliasForm descriptor = new UseAliasForm(option);

        DialogService dialogService = Application.get().getInstance(DialogService.class);
        UIAccess uiAccess = UIAccess.current();
        (component != null ? dialogService.build(component, descriptor) : dialogService.build(descriptor))
            .showAsync()
            .whenCompleteAsync((value, error) -> {
                if (error != null || value == null) {
                    return;
                }

                descriptor.applyTo(option);
                callback.ok(option);
            }, uiAccess);
    }
}
