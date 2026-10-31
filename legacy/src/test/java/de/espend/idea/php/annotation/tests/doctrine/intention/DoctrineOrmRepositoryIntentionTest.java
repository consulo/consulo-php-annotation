package de.espend.idea.php.annotation.tests.doctrine.intention;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention
 */
public class DoctrineOrmRepositoryIntentionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/intention/fixtures";
    }

    public void testThatRepositoryAnnotatorIsAvailableForAnnotation() {
        assertIntentionIsAvailable(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Ent<caret>ity()
                 */
                class Relation
                {
                }""",
            "Add Doctrine repository"
        );
    }

    public void testThatRepositoryAnnotatorIsAvailableForAttribute() {
        assertIntentionIsAvailable(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                #[ORM\\Ent<caret>ity()]class Relation
                {
                }""",
            "Add Doctrine repository"
        );

        assertIntentionIsAvailable(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                #[ORM\\Entity("fo<caret>o")]class Relation
                {
                }""",
            "Add Doctrine repository"
        );
    }

    public void testThatRepositoryAnnotatorForAttributeValueIsAvailable() {
        assertIntentionIsAvailable(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\Entity(<caret>)
                 */
                class Relation
                {
                }""",
            "Add Doctrine repository"
        );
    }

    public void testAttribute() {
        myFixture.configureByText(
            "test.php",
            """
                <?php

                namespace App\\Entity;

                use App\\Entity\\Repository\\UserRepository;
                use Doctrine\\ORM\\Mapping as ORM;

                #[ORM\\Ent<caret>ity()]
                class User {}"""
        );

        String text = invokeAndGetText();
        assertTrue(text.contains("use App\\Entity\\Repository\\UserRepository;"));
        assertTrue(text.contains("#[ORM\\Entity(repositoryClass: UserRepository::class)]"));
    }

    public void testAnnotation() {
        myFixture.configureByText(
            "test.php",
            """
                <?php

                namespace App\\Entity;

                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @ORM\\En<caret>tity()
                 */
                class User {}"""
        );

        String text = invokeAndGetText();
        assertTrue(text.contains("use App\\Entity\\Repository\\UserRepository;"));
        assertTrue(text.contains("@ORM\\Entity(repositoryClass=UserRepository::class)"));
    }

    private String invokeAndGetText() {
        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            new DoctrineOrmRepositoryIntention().invoke(getProject(), getEditor(), psiElement);
        });

        return getEditor().getDocument().getText();
    }
}
