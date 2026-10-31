package de.espend.idea.php.annotation.tests;

import com.intellij.psi.PsiFile;
import com.intellij.util.PlatformIcons;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.AnnotationIconProvider;

public class AnnotationIconProviderTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/fixtures";
    }

    public void testProvidesAnnotationIconForAnnotationFile() {
        PsiFile file = myFixture.configureByFile("AnnotationIconProvider.php");

        assertSame(PlatformIcons.ANNOTATION_TYPE_ICON, new AnnotationIconProvider().getIcon(file, 0));
    }

    public void testDoesNotProvideIconForRegularPhpFile() {
        PsiFile file = myFixture.configureByText(PhpFileType.INSTANCE, "<?php class Foo {}");

        assertNull(new AnnotationIconProvider().getIcon(file, 0));
    }
}
