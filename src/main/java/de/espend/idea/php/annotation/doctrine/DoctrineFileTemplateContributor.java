package de.espend.idea.php.annotation.doctrine;

import consulo.annotation.component.ExtensionImpl;
import consulo.fileTemplate.FileTemplateContributor;
import consulo.fileTemplate.FileTemplateRegistrator;

/**
 * Registers internal Doctrine file templates (was plugin.xml internalFileTemplate)
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class DoctrineFileTemplateContributor implements FileTemplateContributor {
    @Override
    public void register(FileTemplateRegistrator registrator) {
        registrator.registerInternalTemplate("Doctrine Entity Repository");
        registrator.registerInternalTemplate("Doctrine Entity ServiceRepository");
    }
}
