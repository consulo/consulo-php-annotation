package de.espend.idea.php.annotation.tests.doctrine.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmFieldIntention
 */
public class DoctrineOrmFieldIntentionTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/intention/fixtures";
    }

    public void testThatAddDoctrineColumnIsAvailable() {
        assertIntentionIsAvailable(
            PhpFileType.INSTANCE,
            """
                <?php

                class Foobar
                {
                   public $i<caret>d;}""",
            "Add Doctrine column"
        );
    }

    public void testThatAddDoctrineColumnIsAvailableIsInvokedWithResult() {
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
                   public $i<caret>d;
                }"""
        );

        IntentionAction action = myFixture.findSingleIntention("Add Doctrine column");
        myFixture.launchAction(action);

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

    public void testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForNullable() {
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
                   public ?int $foo<caret>bar;
                }"""
        );

        IntentionAction action = myFixture.findSingleIntention("Add Doctrine column");
        myFixture.launchAction(action);

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
                     * @ORM\\Column(type="integer", nullable=true)
                     */public ?int $foobar;
                }"""
        );
    }

    public void testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForAttributes() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                #[Foo]
                class Foobar
                {
                   public $i<caret>d;
                }"""
        );

        IntentionAction action = myFixture.findSingleIntention("Add Doctrine column");
        myFixture.launchAction(action);

        myFixture.checkResult(
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                #[Foo]
                class Foobar
                {
                    #[ORM\\Id]
                    #[ORM\\GeneratedValue(strategy: 'AUTO')]
                    #[ORM\\Column(type: 'integer')]
                    public $id;
                }"""
        );
    }

    public void testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForAttributesWithNullable() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                #[Foo]
                class Foobar
                {
                    #[ORM\\Column(type: 'string')]
                    public $foobar;
                }"""
        );
    }

    public void testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForAttributesForExisting() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                class Foobar
                {
                    #[ORM\\GeneratedValue(strategy: 'AUTO')]
                    public $i<caret>d;
                }"""
        );

        IntentionAction action = myFixture.findSingleIntention("Add Doctrine column");
        myFixture.launchAction(action);

        myFixture.checkResult(
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                class Foobar
                {
                    #[ORM\\Id]
                    #[ORM\\Column(type: 'integer')]
                    #[ORM\\GeneratedValue(strategy: 'AUTO')]
                    public $id;
                }"""
        );
    }

    public void testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForAttributesForExistingA() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                class Foobar
                {
                    public $i<caret>d;

                    /**
                    * @ORM\\Column()
                    */
                    public $createdAt;
                }"""
        );

        IntentionAction action = myFixture.findSingleIntention("Add Doctrine column");
        myFixture.launchAction(action);

        myFixture.checkResult(
            """
                <?php

                use Doctrine\\ORM\\Mapping as ORM;

                class Foobar
                {
                    /**
                     * @ORM\\Id
                     * @ORM\\GeneratedValue(strategy="AUTO")
                     * @ORM\\Column(type="integer")
                     */
                    public $id;

                    /**
                    * @ORM\\Column()
                    */
                    public $createdAt;
                }"""
        );
    }
}
