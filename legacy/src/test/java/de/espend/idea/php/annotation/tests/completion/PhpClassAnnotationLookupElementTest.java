package de.espend.idea.php.annotation.tests.completion;

import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.completion.lookupelements.PhpClassAnnotationLookupElement;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import de.espend.idea.php.annotation.util.PhpElementsUtil;

public class PhpClassAnnotationLookupElementTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/completion/fixtures";
    }

    public void testCustomTypeTextIsRendered() {
        PhpClass phpClass = PhpElementsUtil.getClassInterface(getProject(), "\\My\\Annotations\\All");
        assertNotNull(phpClass);

        LookupElementPresentation presentation = new LookupElementPresentation();
        new PhpClassAnnotationLookupElement(phpClass)
            .withTypeText("Imported annotation")
            .renderElement(presentation);

        assertEquals("Imported annotation", presentation.getTypeText());
    }
}
