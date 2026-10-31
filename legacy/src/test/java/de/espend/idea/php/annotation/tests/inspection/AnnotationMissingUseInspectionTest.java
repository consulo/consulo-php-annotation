package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.inspection.AnnotationMissingUseInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see AnnotationMissingUseInspection
 */
public class AnnotationMissingUseInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
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


                /**
                 * @E<caret>ntity()
                 */
                class Foo
                {
                }""",
            AnnotationMissingUseInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foo\\Entity;

                /**
                 * @E<caret>ntity()
                 */
                class Foo
                {
                }""",
            AnnotationMissingUseInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php

                /**
                 * @\\E<caret>ntity()
                 */
                class Foo
                {
                }""",
            AnnotationMissingUseInspection.MESSAGE
        );
    }

    public void testThatInspectionIsNotDisplayedForClassesWhichDoesNotHaveAValidImportPath() {
        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foo\\Entity;

                /**
                 * @Entity()
                 * @Foo()
                 * @Fo<caret>obar()
                 */
                class Foo
                {
                }""",
            AnnotationMissingUseInspection.MESSAGE
        );
    }

    public void testThatInspectionIsDisplayedForAnnotationClassesWithAlias() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php


                /**
                 * @ORM\\E<caret>ntity()
                 */
                class Foo
                {
                }""",
            AnnotationMissingUseInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foo\\Bar as ORM;

                /**
                 * @ORM\\E<caret>ntity()
                 */
                class Foo
                {
                }""",
            AnnotationMissingUseInspection.MESSAGE
        );
    }

    public void testThatBlacklistedAnnotationDoesNotProvideInpsectionMessage() {
        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php


                /**
                 * @Annot<caret>ation()
                 */
                class Foo
                {
                }""",
            AnnotationMissingUseInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php


                /**
                 * @noinspe<caret>ction()
                 */
                class Foo
                {
                }""",
            AnnotationMissingUseInspection.MESSAGE
        );
    }
}
