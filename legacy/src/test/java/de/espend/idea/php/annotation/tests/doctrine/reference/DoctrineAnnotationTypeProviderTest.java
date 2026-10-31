package de.espend.idea.php.annotation.tests.doctrine.reference;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

public class DoctrineAnnotationTypeProviderTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/reference/fixtures";
    }

    public void testEntityRepositoryClassProvidesReference() {
        assertReferenceMatchOnParent(
            PhpFileType.INSTANCE,
            """
                <?php
                use Doctrine\\ORM\\Mapping as ORM;

                /** @ORM\\Entity(repositoryClass="App\\Repository\\UserRepo<caret>sitory") */
                class User {}""",
            PlatformPatterns.psiElement(PhpClass.class).withName("UserRepository")
        );
    }

    public void testEntityRepositoryClassProvidesCompletion() {
        assertCompletionContains(
            PhpFileType.INSTANCE,
            """
                <?php
                use Doctrine\\ORM\\Mapping as ORM;

                /** @ORM\\Entity(repositoryClass="<caret>") */
                class User {}""",
            "App\\Repository\\UserRepository"
        );
    }
}
