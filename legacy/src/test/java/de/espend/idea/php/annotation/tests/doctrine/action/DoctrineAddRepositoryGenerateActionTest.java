package de.espend.idea.php.annotation.tests.doctrine.action;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.action.DoctrineAddRepositoryGenerateAction
 */
public class DoctrineAddRepositoryGenerateActionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("DoctrineAddRepositoryGenerateAction.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/action/fixtures";
    }

    public void testGenerationForAnnotation() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App\\Entity;
                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Entity()
                 */
                class User
                {<caret>
                }"""
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.DoctrineAddRepositoryGenerateAction");

        myFixture.checkResult(
            """
                <?php
                namespace App\\Entity;
                use App\\Entity\\Repository\\UserRepository;
                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Entity(repositoryClass=UserRepository::class)
                 */
                class User
                {
                }"""
        );
    }

    public void testGenerationForAnnotationInsideDocCommentScope() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App\\Entity;
                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Ent<caret>ity()
                 */
                class User
                {
                }"""
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.DoctrineAddRepositoryGenerateAction");

        myFixture.checkResult(
            """
                <?php
                namespace App\\Entity;
                use App\\Entity\\Repository\\UserRepository;
                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Entity(repositoryClass=UserRepository::class)
                 */
                class User
                {
                }"""
        );
    }
}
