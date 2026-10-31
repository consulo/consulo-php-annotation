package de.espend.idea.php.annotation.tests.doctrine.action;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.action.DoctrinePropertyOrmAnnotationGenerateAction
 */
public class DoctrinePropertyOrmAnnotationGenerateActionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/action/fixtures";
    }

    public void testGeneratesOrmAnnotationForProperty() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                /**
                 * @\\Doctrine\\ORM\\Mapping\\Entity
                 */
                class Existing {}

                class Foobar
                {
                   public $id;<caret>
                }"""
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.PropertyGenerator");

        myFixture.checkResult(
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @\\Doctrine\\ORM\\Mapping\\Entity
                 */
                class Existing {}

                class Foobar
                {
                    /**
                     * @ORM\\Id
                     * @ORM\\GeneratedValue(strategy="AUTO")
                     * @ORM\\Column(type="integer")
                     */public $id;
                }"""
        );
    }
}
