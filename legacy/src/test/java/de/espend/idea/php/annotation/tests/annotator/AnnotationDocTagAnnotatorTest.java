package de.espend.idea.php.annotation.tests.annotator;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagAnnotator;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationDocTagAnnotatorParameter;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import de.espend.idea.php.annotation.util.AnnotationUtil;

import java.util.ArrayList;
import java.util.List;

public class AnnotationDocTagAnnotatorTest extends AnnotationLightCodeInsightFixtureTestCase {
    private final List<PhpAnnotationDocTagAnnotatorParameter> parameters = new ArrayList<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        AnnotationUtil.EP_DOC_TAG_ANNOTATOR.getPoint().registerExtension(
            (PhpAnnotationDocTagAnnotator) parameters::add,
            getTestRootDisposable()
        );
    }

    public void testDelegatesResolvedAnnotationClass() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App;

                class Entity {}

                /** @Entity */
                class Foo {}"""
        );

        myFixture.doHighlighting();

        PhpAnnotationDocTagAnnotatorParameter parameter = findParameter("@Entity");
        assertNotNull(parameter);
        assertNotNull(parameter.getAnnotationClass());
        assertEquals("\\App\\Entity", parameter.getAnnotationClass().getFQN());
    }

    public void testDelegatesUnresolvedAnnotationWithoutClass() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App;

                /** @Unknown */
                class Foo {}"""
        );

        myFixture.doHighlighting();

        PhpAnnotationDocTagAnnotatorParameter parameter = findParameter("@Unknown");
        assertNotNull(parameter);
        assertNull(parameter.getAnnotationClass());
    }

    public void testDoesNotDelegateBlockedDocTag() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                /** @param string $value */
                function foo(string $value): void {}"""
        );

        myFixture.doHighlighting();

        assertNull(findParameter("@param"));
    }

    private PhpAnnotationDocTagAnnotatorParameter findParameter(String docTagName) {
        for (PhpAnnotationDocTagAnnotatorParameter parameter : parameters) {
            if (docTagName.equals(parameter.getPhpDocTag().getName())) {
                return parameter;
            }
        }

        return null;
    }
}
