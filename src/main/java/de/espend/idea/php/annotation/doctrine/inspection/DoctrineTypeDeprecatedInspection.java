package de.espend.idea.php.annotation.doctrine.inspection;

import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.inspection.AnnotationLocalInspectionTool;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jspecify.annotations.Nullable;

/**
 * Check for underlay class deprecations of Column type class from Doctrine
 * <p>
 * Example:
 * - '\Doctrine\ORM\Mapping\Column(type="json_array")'
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class DoctrineTypeDeprecatedInspection extends AnnotationLocalInspectionTool {
    private static final ElementPattern<PsiElement> DOC_IDENTIFIER_PATTERN = PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER);

    public DoctrineTypeDeprecatedInspection() {
        super("DoctrineTypeDeprecatedInspection", "[Annotations] Doctrine column type deprecated");
    }

    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        return new DoctrineTypePropertyVisitor(holder);
    }

    private static class DoctrineTypePropertyVisitor extends PsiElementVisitor {
        private final ProblemsHolder myHolder;

        DoctrineTypePropertyVisitor(ProblemsHolder holder) {
            myHolder = holder;
        }

        @Override
        public void visitElement(PsiElement element) {
            if (!(element instanceof StringLiteralExpression stringLiteralExpression)) {
                return;
            }

            String contents = getContentIfTypeValid(stringLiteralExpression, "\\Doctrine\\ORM\\Mapping\\Column", "type");
            if (contents != null) {
                for (PhpClass columnPhpClass : DoctrineUtil.getColumnTypesTargets(myHolder.getProject(), contents)) {
                    if (!columnPhpClass.isDeprecated()) {
                        continue;
                    }

                    String deprecationMessage = PhpElementsUtil.getClassDeprecatedMessage(columnPhpClass);
                    String message = "[Annotations] " + (deprecationMessage != null ? deprecationMessage : "Field '" + contents + "' is deprecated");

                    myHolder.newProblem(LocalizeValue.of(message))
                        .range(element)
                        .highlightType(ProblemHighlightType.LIKE_DEPRECATED)
                        .create();

                    break;
                }
            }

            super.visitElement(element);
        }
    }

    private static @Nullable String getContentIfTypeValid(StringLiteralExpression stringLiteralExpression, String clazz, String property) {
        if (AnnotationPattern.getAttributesValueReferencesPattern().accepts(stringLiteralExpression)) {
            PsiElement attributeNamePsi = PhpPsiUtil.getPrevSibling(
                stringLiteralExpression,
                sibling -> sibling instanceof PsiWhiteSpace || sibling.getNode().getElementType() == PhpTokenTypes.opCOLON
            );

            if (attributeNamePsi != null
                && attributeNamePsi.getNode().getElementType() == PhpTokenTypes.IDENTIFIER
                && property.equals(attributeNamePsi.getText())) {
                PhpAttribute phpAttribute = PsiTreeUtil.getParentOfType(stringLiteralExpression, PhpAttribute.class);
                if (phpAttribute != null && PhpLangUtil.equalsClassNames(clazz, phpAttribute.getFQN())) {
                    return stringLiteralExpression.getContents();
                }
            }
        }
        else if (stringLiteralExpression.getNode().getElementType() == PhpDocElementTypes.phpDocString) {
            PsiElement propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(stringLiteralExpression, DOC_IDENTIFIER_PATTERN);
            if (propertyName != null && property.equals(propertyName.getText())) {
                PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(stringLiteralExpression, PhpDocTag.class);
                if (phpDocTag != null) {
                    PhpDocTagAnnotation phpDocAnnotationContainer = AnnotationUtil.getPhpDocAnnotationContainer(phpDocTag);
                    if (phpDocAnnotationContainer != null
                        && PhpLangUtil.equalsClassNames(phpDocAnnotationContainer.getPhpClass().getFQN(), clazz)) {
                        return stringLiteralExpression.getContents();
                    }
                }
            }
        }

        return null;
    }
}
