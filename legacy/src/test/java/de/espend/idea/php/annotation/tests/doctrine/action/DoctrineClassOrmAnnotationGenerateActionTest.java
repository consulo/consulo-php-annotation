package de.espend.idea.php.annotation.tests.doctrine.action;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.doctrine.action.DoctrineClassGeneratorAction;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see DoctrineClassGeneratorAction
 * @see de.espend.idea.php.annotation.doctrine.action.DoctrineClassOrmAnnotationGenerateAction
 */
public class DoctrineClassOrmAnnotationGenerateActionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/action/fixtures";
    }

    public void testThatThatEntityClassIsGeneratedForAnnotations() {
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

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.ClassGenerator");

        myFixture.checkResult(
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @\\Doctrine\\ORM\\Mapping\\Entity
                 */
                class Existing {}

                /**
                 * @ORM\\Entity
                 * @ORM\\Table(name="foobar")
                 */
                class Foobar
                {
                   public $id;
                }"""
        );
    }

    public void testThatThatEntityClassIsGeneratedForAttribute() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                class Foobar
                {
                   #[Dummy]
                   public $id;<caret>
                }"""
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.ClassGenerator");

        myFixture.checkResult(
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                #[ORM\\Entity]
                #[ORM\\Table(name: 'foobar')]
                class Foobar
                {
                   #[Dummy]
                   public $id;
                }"""
        );
    }

    public void testThatThatEntityClassIsGeneratedForAttributeWithRepository() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App\\Entity;

                class Car
                {
                   #[Dummy]
                   public $id;<caret>
                }"""
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.ClassGenerator");

        myFixture.checkResult(
            """
                <?php
                namespace App\\Entity;

                use App\\Entity\\CarRepository;
                use Doctrine\\ORM\\Mapping as ORM;

                #[ORM\\Entity(repositoryClass: App\\Entity\\CarRepository::class)]
                #[ORM\\Table(name: 'car')]
                class Car
                {
                   #[Dummy]
                   public $id;
                }"""
        );
    }
}
