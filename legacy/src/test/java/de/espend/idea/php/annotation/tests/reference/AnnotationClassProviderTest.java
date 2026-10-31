package de.espend.idea.php.annotation.tests.reference;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

public class AnnotationClassProviderTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/completion/fixtures";
    }

    public void testTargetEntityProvidesClassReference() {
        assertReferenceMatchOnParent(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App;

                use My\\Annotations\\All;

                class Target {}

                /** @All(targetEntity="\\App\\Tar<caret>get") */
                class Foo {}""",
            PlatformPatterns.psiElement(PhpClass.class).withName("Target")
        );
    }
}
