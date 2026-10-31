package de.espend.idea.php.annotation.inspection;

import com.jetbrains.php.lang.PhpLanguage;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.localize.LocalizeValue;
import org.jspecify.annotations.Nullable;

/**
 * Common registration metadata of the plugin inspections (was plugin.xml localInspection attributes)
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public abstract class AnnotationLocalInspectionTool extends LocalInspectionTool {
    private final String myShortName;
    private final LocalizeValue myDisplayName;

    protected AnnotationLocalInspectionTool(String shortName, String displayName) {
        myShortName = shortName;
        myDisplayName = LocalizeValue.localizeTODO(displayName);
    }

    @Override
    public String getShortName() {
        return myShortName;
    }

    @Override
    public LocalizeValue getDisplayName() {
        return myDisplayName;
    }

    @Override
    public LocalizeValue getGroupDisplayName() {
        return LocalizeValue.localizeTODO("Annotation");
    }

    @Override
    public LocalizeValue[] getGroupPath() {
        return new LocalizeValue[]{LocalizeValue.localizeTODO("PHP"), getGroupDisplayName()};
    }

    @Override
    public @Nullable Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Override
    public boolean runForWholeFile() {
        return true;
    }
}
