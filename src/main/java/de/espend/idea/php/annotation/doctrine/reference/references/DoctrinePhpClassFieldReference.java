package de.espend.idea.php.annotation.doctrine.reference.references;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpAttributesList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementResolveResult;
import consulo.language.psi.PsiPolyVariantReferenceBase;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import de.espend.idea.php.annotation.dict.PhpDocCommentAnnotation;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrinePhpClassFieldReference extends PsiPolyVariantReferenceBase<PsiElement> {
    private final PhpClass myPhpClass;
    private final String myContent;

    public DoctrinePhpClassFieldReference(StringLiteralExpression psiElement, PhpClass phpClass) {
        super(psiElement);
        myPhpClass = phpClass;
        myContent = psiElement.getContents();
    }

    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        List<Field> fields = new ArrayList<>();
        for (Field field : myPhpClass.getFields()) {
            if (!field.isConstant() && myContent.equals(field.getName())) {
                fields.add(field);
            }
        }

        return PsiElementResolveResult.createResults(fields);
    }

    @Override
    public Object[] getVariants() {
        PhpClass entity = PsiTreeUtil.getParentOfType(getElement(), PhpClass.class);

        List<LookupElementBuilder> lookupElements = new ArrayList<>();
        for (Field field : myPhpClass.getFields()) {
            if (field.isConstant()) {
                continue;
            }

            lookupElements.add(attachLookupInformation(field, LookupElementBuilder.createWithIcon(field), entity));
        }

        return lookupElements.toArray();
    }

    private LookupElementBuilder attachLookupInformation(Field field, LookupElementBuilder lookupElement, @Nullable PhpClass entity) {
        LookupElementBuilder result = lookupElement;
        boolean matchForeignType = entity != null && fieldMatchesEntity(field.getProject(), field, entity.getFQN());

        // get some more presentable completion information
        PhpDocComment docBlock = field.getDocComment();
        if (docBlock != null) {
            PhpDocCommentAnnotation annotationContainer = AnnotationUtil.getPhpDocCommentAnnotationContainer(docBlock);
            if (annotationContainer == null) {
                return result;
            }

            // search column type
            PhpDocTagAnnotation column = annotationContainer.getPhpDocBlock("Doctrine\\ORM\\Mapping\\Column");
            if (column != null) {
                String value = column.getPropertyValue("type");
                if (value != null) {
                    result = result.withTypeText(value, true);
                }
            }

            // search for relations
            PhpDocTagAnnotation relation = annotationContainer.getFirstPhpDocBlock(DoctrineUtil.DOCTRINE_RELATION_FIELDS);
            if (relation != null) {
                result = attachRelationInformation(result, relation, matchForeignType);
            }

            return result;
        }

        List<PhpAttributesList> attributeLists;
        PsiElement fieldParent = field.getParent();
        if (field instanceof Parameter) {
            // promoted constructor property (PhpStorm: PhpPromotedFieldParameterImpl)
            attributeLists = PsiTreeUtil.getChildrenOfTypeAsList(field, PhpAttributesList.class);
        }
        else if (fieldParent instanceof PhpPsiElement) {
            attributeLists = PsiTreeUtil.getChildrenOfTypeAsList(fieldParent, PhpAttributesList.class);
        }
        else {
            attributeLists = Collections.emptyList();
        }

        for (PhpAttributesList attributesList : attributeLists) {
            for (PhpAttribute attribute : getAttributes(attributesList, "\\Doctrine\\ORM\\Mapping\\Column")) {
                String value = PhpElementsUtil.getAttributeArgumentStringByName(attribute, "type");
                if (value != null) {
                    result = result.withTypeText(value, true);
                }
            }

            for (String relationClass : DoctrineUtil.DOCTRINE_RELATION_FIELDS) {
                for (PhpAttribute attribute : getAttributes(attributesList, relationClass)) {
                    result = attachRelationInformation(result, attribute, matchForeignType);
                }
            }
        }

        return result;
    }

    private static boolean fieldMatchesEntity(Project project, Field field, String entityFqn) {
        for (String type : PhpIndex.getInstance(project).completeType(project, field.getType(), new HashSet<>()).getTypes()) {
            if (type.equals(entityFqn) || type.equals(entityFqn + "[]")) {
                return true;
            }
        }

        return false;
    }

    private static LookupElementBuilder attachRelationInformation(LookupElementBuilder lookupElement, PhpDocTagAnnotation relation, boolean matchForeignType) {
        LookupElementBuilder result = lookupElement;

        String value = relation.getPropertyValue("targetEntity");
        if (value != null) {
            result = result
                .withTypeText(StringUtils.stripStart(value, "\\"), true)
                .withBoldness(matchForeignType);
        }

        return result.withTailText("(" + relation.getPhpClass().getName() + ")", true);
    }

    private static LookupElementBuilder attachRelationInformation(LookupElementBuilder lookupElement, PhpAttribute relation, boolean matchForeignType) {
        String value = PhpElementsUtil.getAttributeArgumentStringByName(relation, "targetEntity");
        if (value == null) {
            return lookupElement;
        }

        return lookupElement
            .withTypeText(StringUtils.stripStart(value, "\\"), true)
            .withBoldness(matchForeignType);
    }

    /**
     * PhpStorm: {@code PhpAttributesList#getAttributes(String fqn)}
     */
    private static List<PhpAttribute> getAttributes(PhpAttributesList attributesList, String fqn) {
        // TODO consulo-php: PhpAttributesList#getAttributes(String) missing
        String normalizedFqn = fqn.startsWith("\\") ? fqn : "\\" + fqn;

        List<PhpAttribute> attributes = new ArrayList<>();
        for (PhpAttribute attribute : attributesList.getAttributes()) {
            if (normalizedFqn.equalsIgnoreCase(attribute.getFQN())) {
                attributes.add(attribute);
            }
        }

        return attributes;
    }
}
