package de.espend.idea.php.annotation.tests.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import de.espend.idea.php.annotation.util.AnnotationUtil;

import java.util.ArrayList;
import java.util.List;

public class AnnotationPropertyValueReferenceContributorTest extends AnnotationLightCodeInsightFixtureTestCase {
    private final List<AnnotationPropertyParameter> parameters = new ArrayList<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
        AnnotationUtil.EXTENSION_POINT_REFERENCES.getPoint().registerExtension(
            (PhpAnnotationReferenceProvider) (property, parameter) -> {
                parameters.add(property);
                return PsiReference.EMPTY_ARRAY;
            },
            getTestRootDisposable()
        );
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/completion/fixtures";
    }

    public void testDelegatesNamedAnnotationPropertyToExtensions() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                use My\\Annotations\\All;

                /** @All(strategy="AU<caret>TO") */
                class Foo {}"""
        );

        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        while (element != null) {
            element.getReferences();
            element = element.getParent();
        }

        AnnotationPropertyParameter property = null;
        for (AnnotationPropertyParameter parameter : parameters) {
            if ("strategy".equals(parameter.getPropertyName())) {
                property = parameter;
                break;
            }
        }

        assertNotNull(property);
        assertEquals(AnnotationPropertyParameter.Type.PROPERTY_VALUE, property.getType());
        assertEquals("\\My\\Annotations\\All", property.getPhpClass().getFQN());
    }
}
