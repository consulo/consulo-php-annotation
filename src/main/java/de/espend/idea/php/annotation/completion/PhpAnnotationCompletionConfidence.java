package de.espend.idea.php.annotation.completion;

import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocStubElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.patterns.PhpPatterns;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionConfidence;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import consulo.util.lang.ThreeState;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl(id = "asTrue", order = "first")
public class PhpAnnotationCompletionConfidence extends CompletionConfidence {
    @Override
    public ThreeState shouldSkipAutopopup(PsiElement contextElement, PsiFile psiFile, int offset) {
        if (!(psiFile instanceof PhpFile)) {
            return ThreeState.UNSURE;
        }

        PsiElement context = contextElement.getContext();
        if (context instanceof StringLiteralExpression) {
            // foo="<|>"
            if (PhpPatterns.psiElement(PhpDocElementTypes.phpDocString).accepts(context)) {
                return ThreeState.NO;
            }
        }
        else if (context instanceof PhpDocComment) {
            // * <|>
            if (PhpPatterns.psiElement().afterLeafSkipping(
                PhpPatterns.psiElement(PsiWhiteSpace.class),
                PhpPatterns.psiElement(PhpDocTokenTypes.DOC_LEADING_ASTERISK)
            ).accepts(contextElement)) {
                return ThreeState.NO;
            }
        }
        else if (context instanceof PhpPsiElement) {
            // TODO consulo-php: com.jetbrains.php.lang.psi.elements.impl.PhpPsiElementImpl missing; PhpPsiElement interface used instead

            // @Foo(<|>)
            if (PhpPatterns.psiElement(PhpDocElementTypes.phpDocAttributeList).accepts(context)) {
                return ThreeState.NO;
            }

            // @<|>
            if (PhpPatterns.psiElement(PhpDocStubElementTypes.phpDocTag).accepts(context)) {
                return ThreeState.NO;
            }
        }

        return ThreeState.UNSURE;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
