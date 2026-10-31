package de.espend.idea.php.annotation.completion;

import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import de.espend.idea.php.annotation.dict.AnnotationPropertyEnum;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.util.AnnotationUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class PhpAnnotationTypeCompletionProvider implements PhpAnnotationCompletionProvider {
    private static final Pattern TARGET_PATTERN = Pattern.compile("\"(\\w+)\"");

    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, AnnotationCompletionProviderParameter completionParameter) {
        String propertyName = annotationPropertyParameter.getPropertyName();
        if (annotationPropertyParameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_VALUE || propertyName == null) {
            return;
        }

        Set<String> values = new HashSet<>();

        AnnotationUtil.visitAttributes(annotationPropertyParameter.getPhpClass(), (attributeName, type, target) -> {
            if (propertyName.equals(attributeName)) {
                if (AnnotationPropertyEnum.fromString(type) == AnnotationPropertyEnum.BOOLEAN) {
                    values.addAll(List.of("false", "true"));
                }

                // @Enum({"AUTO", "SEQUENCE"})
                if (target instanceof Field field) {
                    PhpDocComment docComment = field.getDocComment();
                    if (docComment != null) {
                        for (PhpDocTag phpDocTag : docComment.getTagElementsByName("@Enum")) {
                            PsiElement phpDocAttrList = phpDocTag.getFirstPsiChild();
                            if (phpDocAttrList != null) {
                                Matcher matcher = TARGET_PATTERN.matcher(phpDocAttrList.getText());
                                while (matcher.find()) {
                                    values.add(matcher.group(1));
                                }
                            }
                        }
                    }
                }
            }
        });

        for (String s : values) {
            completionParameter.getResult().addElement(LookupElementBuilder.create(s));
        }
    }
}
