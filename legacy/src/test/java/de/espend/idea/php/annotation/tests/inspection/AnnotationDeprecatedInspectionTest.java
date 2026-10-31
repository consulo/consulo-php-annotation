package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.inspection.AnnotationDeprecatedInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

public class AnnotationDeprecatedInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes_deprecated.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/inspection/fixtures";
    }

    public void testThatInspectionIsDisplayedForAnnotationClasses() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php
                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\E<caret>ntity()
                 */
                class Foo
                {
                }""",
            AnnotationDeprecatedInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Foo<caret>bar()
                 */
                class Foo
                {
                }""",
            AnnotationDeprecatedInspection.MESSAGE
        );
    }
}
