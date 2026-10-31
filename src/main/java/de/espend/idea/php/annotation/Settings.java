package de.espend.idea.php.annotation;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.project.Project;
import consulo.util.xml.serializer.XmlSerializerUtil;
import jakarta.inject.Singleton;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@State(name = "EspendPhpAnnotationSetting", storages = @Storage("espend_php_annotation.xml"))
public class Settings implements PersistentStateComponent<Settings> {
    @Override
    public Settings getState() {
        return this;
    }

    @Override
    public void loadState(Settings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static Settings getInstance(Project project) {
        return project.getInstance(Settings.class);
    }
}
