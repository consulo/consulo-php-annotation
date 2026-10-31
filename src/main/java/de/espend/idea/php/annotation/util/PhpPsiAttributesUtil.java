package de.espend.idea.php.annotation.util;

import consulo.codeEditor.Editor;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.pattern.PsiElementPattern;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Helpers for PHP 8 Attributes psi access
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpPsiAttributesUtil {
    @Nullable
    public static String getAttributeValueByNameAsStringWithClassConstant(PhpAttribute attribute, String attributeName) {
        PsiElement nextSibling = findAttributeByName(attribute, attributeName);

        if (nextSibling instanceof StringLiteralExpression) {
            String contents = ((StringLiteralExpression) nextSibling).getContents();
            if (StringUtils.isNotBlank(contents)) {
                return contents;
            }
        } else if(nextSibling instanceof ClassConstantReference) {
            return resolveClassReferenceValue((ClassConstantReference) nextSibling);
        }

        return null;
    }

    /**
     * Workaround to find given attribute: "#[Route('/attributesWithoutName', name: "")]" as attribute iteration given the index as "int" but not the key as name
     */
    @Nullable
    private static PsiElement findAttributeByName(PhpAttribute attribute, String attributeName) {
        ParameterList parameterList = PsiTreeUtil.findChildOfType(attribute, ParameterList.class);
        if (parameterList == null) {
            return null;
        }

        Collection<PsiElement> childrenOfTypeAsList = getChildrenOfTypeAsList(parameterList, getAttributeColonPattern(attributeName));

        if (childrenOfTypeAsList.isEmpty()) {
            return null;
        }

        PsiElement colon = childrenOfTypeAsList.iterator().next();

        return PhpPsiUtil.getNextSibling(colon, psiElement -> psiElement instanceof PsiWhiteSpace);
    }

    /**
     * "#[Route('/path', name: 'attributes_action')]"
     */
    private static PsiElementPattern.Capture<PsiElement> getAttributeColonPattern(String name) {
        return PlatformPatterns.psiElement().withElementType(
            PhpTokenTypes.opCOLON
        ).afterLeaf(PlatformPatterns.psiElement().withElementType(PhpTokenTypes.IDENTIFIER).withText(name));
    }

    @Nullable
    private static String resolveClassReferenceValue(ClassConstantReference nextSibling) {
        if (nextSibling.getClassReference() instanceof ClassReference classReference) {
            String fqn = classReference.getFQN();

            if (StringUtils.isNotBlank(fqn)) {
                return fqn;
            }
        }

        return null;
    }

    private static <T extends PsiElement> Collection<T> getChildrenOfTypeAsList(@Nullable PsiElement element, ElementPattern<T> pattern) {

        Collection<T> collection = new ArrayList<>();

        if (element == null) {
            return collection;
        }

        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (pattern.accepts(child)) {
                //noinspection unchecked
                collection.add((T)child);
            }
        }

        return collection;
    }

    public static void insertNamedArgumentForAttribute(Editor editor, PhpAttribute phpAttribute, String namedArgument, String value) {
        PsiElement lastChild = phpAttribute.getLastChild();
        if (lastChild == null) {
            return;
        }

        if (lastChild instanceof ClassReference) {
            editor.getDocument().insertString(lastChild.getTextRange().getEndOffset(), MessageFormat.format("({0}: {1})", namedArgument, value));
        } else {
            String format = MessageFormat.format("{0}: {1}", namedArgument, value);

            if (!phpAttribute.getArguments().isEmpty()) {
                format = ", " + format;
            }

            editor.getDocument().insertString(lastChild.getTextRange().getStartOffset(), format);
        }
    }
}
