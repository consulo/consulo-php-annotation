package de.espend.idea.php.annotation.util;

import consulo.util.lang.Pair;
import consulo.language.psi.PsiElement;
import consulo.application.util.function.Processor;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpDocTagAnnotationVisitorUtil {
    public static void visitElement(PhpFile psiFile, Processor<Pair<String, PsiElement>> processor) {
        for (PhpNamedElement topLevelElement : psiFile.getTopLevelDefs().values()) {
            if (topLevelElement instanceof PhpClass clazz) {
                PhpDocComment docComment = clazz.getDocComment();
                if (docComment != null) {
                    PhpDocUtil.consumeTagElementsByName(docComment, null, docTag -> visitPhpDocTag(docTag, processor));
                }

                for (PhpAttribute attribute : clazz.getAttributes()) {
                    String fqn = attribute.getFQN();
                    if (fqn != null && !fqn.isEmpty()) {
                        processor.process(Pair.create(StringUtils.stripStart(fqn, "\\"), attribute));
                    }
                }
            }
        }
    }

    private static void visitPhpDocTag(PhpDocTag phpDocTag, Processor<Pair<String, PsiElement>> processor) {
        // "@var" and user non-related tags don't need an action
        String name = phpDocTag.getName();
        if (AnnotationUtil.isBlockedAnnotationTag(name)) {
            return;
        }

        String annotationFqnName = StringUtils.stripStart(getClassNameReference(phpDocTag, AnnotationUtil.getUseImportMap((PsiElement) phpDocTag)), "\\");

        if (StringUtils.isNotBlank(annotationFqnName)) {
            processor.process(Pair.create(annotationFqnName, phpDocTag));
        }
    }

    @Nullable
    private static String getClassNameReference(PhpDocTag phpDocTag, Map<String, String> useImports) {

        if(useImports.isEmpty()) {
            return null;
        }

        String annotationName = phpDocTag.getName();
        if(StringUtils.isBlank(annotationName)) {
            return null;
        }

        if(annotationName.startsWith("@")) {
            annotationName = annotationName.substring(1);
        }

        String className = annotationName;
        String subNamespaceName = "";
        if(className.contains("\\")) {
            className = className.substring(0, className.indexOf("\\"));
            subNamespaceName = annotationName.substring(className.length());
        }

        if(!useImports.containsKey(className)) {
            return null;
        }

        // normalize name
        String annotationFqnName = useImports.get(className) + subNamespaceName;
        if(!annotationFqnName.startsWith("\\")) {
            annotationFqnName = "\\" + annotationFqnName;
        }

        return annotationFqnName;
    }
}