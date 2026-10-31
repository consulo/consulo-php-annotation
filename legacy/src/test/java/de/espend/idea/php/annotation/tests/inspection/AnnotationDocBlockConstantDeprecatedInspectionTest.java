package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.inspection.AnnotationDocBlockConstantDeprecatedInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @see AnnotationDocBlockConstantDeprecatedInspection
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationDocBlockConstantDeprecatedInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/inspection/fixtures";
    }

    public void testTheClassConstantProvidesNotificationForDeprecatedClassUsage() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php
                use Foobar\\Bar\\FooBarDeprecated;
                /**
                 * @Foobar(type=FooBarDeprecated::cl<caret>ass)
                 */
                class Foo
                {
                }""",
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foobar\\Bar\\FooBar;
                /**
                 * @Foobar(type=FooBarDeprecated::cl<caret>ass)
                 */
                class Foo
                {
                }""",
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE
        );
    }

    public void testTheConstantProvidesNotificationForDeprecatedUsage() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php
                use Foobar\\Bar\\FooBarDeprecated;
                /**
                 * @Foobar(type=FooBarDeprecated::I_AM_DEP<caret>RECATED)
                 */
                class Foo
                {
                }""",
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foobar\\Bar\\FooBarDeprecated;
                /**
                 * @Foobar(type=FooBarDeprecated::I_AM_NOT_DEPRECATED)
                 */
                class Foo
                {
                }""",
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE
        );
    }
}
