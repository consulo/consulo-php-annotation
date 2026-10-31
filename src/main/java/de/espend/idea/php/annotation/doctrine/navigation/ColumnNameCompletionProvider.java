package de.espend.idea.php.annotation.doctrine.navigation;

import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpAttributesList;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.util.PsiTreeUtil;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.icon.PhpAnnotationIconGroup;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * "Column(name="field_data2", type="integer")"
 * private $FieldData2;
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class ColumnNameCompletionProvider implements PhpAnnotationCompletionProvider {
    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, AnnotationCompletionProviderParameter completionParameter) {
        if (!"name".equals(annotationPropertyParameter.getPropertyName())) {
            return;
        }

        String fqn = annotationPropertyParameter.getPhpClass().getFQN();
        boolean isColumn = PhpLangUtil.equalsClassNames(fqn, "\\Doctrine\\ORM\\Mapping\\Column")
            || PhpLangUtil.equalsClassNames(fqn, "\\Doctrine\\ORM\\Mapping\\JoinColumn")
            || PhpLangUtil.equalsClassNames(fqn, "\\Doctrine\\ORM\\Mapping\\InverseJoinColumn");

        if (!isColumn) {
            return;
        }

        PhpDocComment phpDocComment = PsiTreeUtil.getParentOfType(annotationPropertyParameter.getElement(), PhpDocComment.class);
        if (phpDocComment != null) {
            PhpPsiElement classField = phpDocComment.getNextPsiSibling();
            if (classField != null && classField.getNode().getElementType() == PhpElementTypes.CLASS_FIELDS) {
                PhpNamedElement phpNamedElement = PsiTreeUtil.getChildOfType(classField, PhpNamedElement.class);
                addColumnName(phpNamedElement, completionParameter);
            }
        }

        PhpAttributesList parentOfType = PsiTreeUtil.getParentOfType(annotationPropertyParameter.getElement(), PhpAttributesList.class);
        if (parentOfType != null && parentOfType.getParent() instanceof PhpPsiElement phpPsiElement) {
            // promoted constructor property (PhpStorm: PhpPromotedFieldParameterImpl)
            PhpNamedElement phpNamedElement = phpPsiElement instanceof Parameter && phpPsiElement instanceof Field field
                ? field
                : PsiTreeUtil.getChildOfType(phpPsiElement, PhpNamedElement.class);

            addColumnName(phpNamedElement, completionParameter);
        }
    }

    private static void addColumnName(@Nullable PhpNamedElement phpNamedElement, AnnotationCompletionProviderParameter completionParameter) {
        if (phpNamedElement == null) {
            return;
        }

        String name = phpNamedElement.getName();
        if (StringUtils.isBlank(name)) {
            return;
        }

        completionParameter.getResult().addElement(
            LookupElementBuilder.create(underscoreColumnName(name)).withIcon(PhpAnnotationIconGroup.doctrine())
        );
    }

    private static String underscoreColumnName(String name) {
        return StringUtils.capitalize(name).replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.getDefault());
    }
}
