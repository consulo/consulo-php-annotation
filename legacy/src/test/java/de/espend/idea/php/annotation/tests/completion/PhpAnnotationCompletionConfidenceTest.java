package de.espend.idea.php.annotation.tests.completion;

import com.intellij.psi.PsiElement;
import com.intellij.util.ThreeState;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.completion.PhpAnnotationCompletionConfidence;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

public class PhpAnnotationCompletionConfidenceTest extends AnnotationLightCodeInsightFixtureTestCase {
    private final PhpAnnotationCompletionConfidence confidence = new PhpAnnotationCompletionConfidence();

    public void testAllowsAutopopupInsideAnnotationString() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                /** @Route(name="fo<caret>o") */
                class Controller {}"""
        );

        assertEquals(ThreeState.NO, shouldSkipAutopopup());
    }

    public void testKeepsDefaultConfidenceInsidePhpString() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                $value = "fo<caret>o";"""
        );

        assertEquals(ThreeState.UNSURE, shouldSkipAutopopup());
    }

    private ThreeState shouldSkipAutopopup() {
        PsiElement contextElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        if (contextElement == null) {
            contextElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1);
        }
        if (contextElement == null) {
            throw new IllegalStateException("No PSI element at caret");
        }

        return confidence.shouldSkipAutopopup(
            getEditor(),
            contextElement,
            myFixture.getFile(),
            myFixture.getCaretOffset()
        );
    }
}
