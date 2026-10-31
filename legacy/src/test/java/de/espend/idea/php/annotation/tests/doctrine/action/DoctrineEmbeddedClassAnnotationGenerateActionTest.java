package de.espend.idea.php.annotation.tests.doctrine.action;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.action.DoctrineEmbeddedClassAnnotationGenerateAction
 */
public class DoctrineEmbeddedClassAnnotationGenerateActionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/action/fixtures";
    }

    public void testThatThatEmbeddableClassIsGeneratedForAnnotations() {
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
                <caret>}"""
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Embedded.ClassGenerator");

        myFixture.checkResult(
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                /**
                 * @\\Doctrine\\ORM\\Mapping\\Entity
                 */
                class Existing {}

                /**
                 * @ORM\\Embeddable
                 */
                class Foobar
                {
                }"""
        );
    }

    public void testThatThatEmbeddableClassIsGeneratedForAttributes() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                #[Foo]
                class Foobar
                {
                <caret>}"""
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Embedded.ClassGenerator");

        myFixture.checkResult(
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                #[ORM\\Embeddable]
                #[Foo]
                class Foobar
                {
                }"""
        );
    }
}
