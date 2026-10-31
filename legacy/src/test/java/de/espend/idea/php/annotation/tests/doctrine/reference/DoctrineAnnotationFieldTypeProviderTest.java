package de.espend.idea.php.annotation.tests.doctrine.reference;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

public class DoctrineAnnotationFieldTypeProviderTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/reference/fixtures";
    }

    public void testColumnTypeProvidesCompletionAndReference() {
        String annotation = """
            <?php
            use Doctrine\\ORM\\Mapping as ORM;

            class User
            {
                /** @ORM\\Column(type="my_<caret>type") */
                private $name;
            }""";

        assertCompletionContains(PhpFileType.INSTANCE, annotation, "my_type");
        assertReferenceMatchOnParent(
            PhpFileType.INSTANCE,
            annotation,
            PlatformPatterns.psiElement(PhpClass.class).withName("MyType")
        );
    }
}
