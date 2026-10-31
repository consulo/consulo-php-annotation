package de.espend.idea.php.annotation.tests.doctrine.inspection;

import de.espend.idea.php.annotation.doctrine.inspection.RepositoryClassInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see RepositoryClassInspection
 */
public class RepositoryClassInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/inspection/fixtures";
    }

    public void testThatInspectionForMissingClassIsProvided() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Entity(repositoryClass="Foo<caret>bar")
                 */
                class Foo
                {
                }""",
            RepositoryClassInspection.MESSAGE
        );

        assertLocalInspectionContains(
            "test.php",
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Entity("Foobar", repositoryClass="Foo<caret>bar")
                 */
                class Foo
                {
                }""",
            RepositoryClassInspection.MESSAGE
        );
    }

    public void testThatExistingClassIsNotHighlighted() {
        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Entity(repositoryClass="Foob<caret>ar\\Foo")
                 */
                class Foo
                {
                }""",
            RepositoryClassInspection.MESSAGE
        );
    }

    public void testThatExistingClassInSameNamespaceIsNotHighlighted() {
        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php

                namespace Foobar;use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Entity(repositoryClass="F<caret>oo")
                 */
                class Bar
                {
                }""",
            RepositoryClassInspection.MESSAGE
        );
    }
}
