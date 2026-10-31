package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.inspection.AnnotationDocBlockClassConstantNotFoundInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @see AnnotationDocBlockClassConstantNotFoundInspection
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationDocBlockClassConstantNotFoundInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/inspection/fixtures";
    }

    public void testThatClassConstantProvideMissingUseHighlight() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php
                use Foo;
                /**
                 * @Foobar(type=Foo\\Unknown::cl<caret>ass)
                 */
                class Foo
                {
                }""",
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                /**
                 * @Foobar(type=\\Foobar\\Bar\\FooBar::cl<caret>ass)
                 */
                class Foo
                {
                }""",
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foobar\\Bar\\FooBar;
                /**
                 * @Foobar(type=FooBar::cl<caret>ass)
                 */
                class Foo
                {
                }""",
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foobar\\Bar;
                /**
                 * @Foobar(type=FooBar\\FooBar::cl<caret>ass)
                 */
                class Foo
                {
                }""",
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE
        );
    }
}
