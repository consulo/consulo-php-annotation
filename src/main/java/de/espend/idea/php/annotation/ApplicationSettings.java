package de.espend.idea.php.annotation;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.Application;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.util.xml.serializer.XmlSerializerUtil;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import de.espend.idea.php.annotation.extension.PhpAnnotationUseAlias;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@Singleton
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
@State(name = "PhpAnnotationsPlugin", storages = @Storage("PhpAnnotationsPlugin.xml"))
public class ApplicationSettings implements PersistentStateComponent<ApplicationSettings> {
    public boolean appendRoundBracket = true;

    public List<UseAliasOption> useAliasOptions = new ArrayList<>();

    /**
     * First user change, so that can provide defaults
     */
    public boolean provideDefaults = true;

    @Override
    public ApplicationSettings getState() {
        return this;
    }

    @Override
    public void loadState(ApplicationSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static ApplicationSettings getInstance() {
        return Application.get().getInstance(ApplicationSettings.class);
    }

    public static Collection<UseAliasOption> getDefaultUseAliasOption() {
        List<UseAliasOption> options = new ArrayList<>();
        options.add(new UseAliasOption("Symfony\\Component\\Validator\\Constraints", "Assert", true));
        options.add(new UseAliasOption("Doctrine\\ORM\\Mapping", "ORM", true));
        options.add(new UseAliasOption("JMS\\DiExtraBundle\\Annotation", "DI", true));
        options.add(new UseAliasOption("JMS\\Serializer\\Annotation", "Serializer", true));
        options.add(new UseAliasOption("Gedmo\\Mapping\\Annotation", "Gedmo", true));
        options.add(new UseAliasOption("Vich\\UploaderBundle\\Mapping\\Annotation", "Vich", true));
        options.add(new UseAliasOption("FOS\\RestBundle\\Controller\\Annotations", "Rest", true));
        options.add(new UseAliasOption("Swagger\\Annotations", "SWG", true));
        options.add(new UseAliasOption("OpenApi\\Annotations", "OA", true));
        options.add(new UseAliasOption("OpenApi\\Attributes", "OA", true));
        options.add(new UseAliasOption("Sunrise\\Http\\Router\\Annotation", "Routing", true));
        options.add(new UseAliasOption("Sunrise\\Symfony\\OpenApi\\Annotation", "OpenApi", true));

        for (PhpAnnotationUseAlias extension : AnnotationUtil.EP_USE_ALIASES.getExtensionList()) {
            for (Map.Entry<String, String> entry : extension.getAliases().entrySet()) {
                options.add(new UseAliasOption(entry.getValue(), entry.getKey(), true));
            }
        }

        return options;
    }

    public static Collection<UseAliasOption> getUseAliasOptionsWithDefaultFallback() {
        ApplicationSettings settings = getInstance();
        if (settings.provideDefaults && settings.useAliasOptions.isEmpty()) {
            return getDefaultUseAliasOption();
        }

        return settings.useAliasOptions;
    }
}
