package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.doctrine.inspection.DoctrineTypeDeprecatedInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see DoctrineTypeDeprecatedInspection
 */
public class DoctrineTypeDeprecatedInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("DoctrineTypeDeprecatedInspection.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/inspection/fixtures";
    }

    public void testThatDeprecatedInspectionIsDisplayedDoctrineColumnTypes() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php
                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Column(type="json<caret>_array")
                 */
                class Foo
                {
                }""",
            "[Annotations] Deprecated: Use JsonType instead"
        );

        assertLocalInspectionIsEmpty(
            "test.php",
            """
                <?php
                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Column(type="json<caret>")
                 */
                class Foo
                {
                }"""
        );
    }

    public void testThatDeprecatedInspectionIsDisplayedDoctrineColumnTypesForAttribute() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php
                use Doctrine\\ORM\\Mapping as ORM;

                #[ORM\\Column(type: 'json<caret>_array')]
                class Foo
                {
                }""",
            "[Annotations] Deprecated: Use JsonType instead"
        );

        assertLocalInspectionIsEmpty(
            "test.php",
            """
                <?php
                use Doctrine\\ORM\\Mapping as ORM;

                #[ORM\\Foobar(type: 'json<caret>_array')]
                class Foo
                {
                }"""
        );
    }
}
