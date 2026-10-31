package de.espend.idea.php.annotation.doctrine.action;

import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import consulo.codeEditor.Editor;
import consulo.language.editor.action.CodeInsightAction;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import de.espend.idea.php.annotation.dict.PhpDocCommentAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.icon.PhpAnnotationIconGroup;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public abstract class DoctrineClassGeneratorAction extends CodeInsightAction {
    private static final ElementPattern<PsiElement> INSIDE_PHP_CLASS_PATTERN = PlatformPatterns.psiElement().inside(PhpClass.class);

    protected DoctrineClassGeneratorAction(String text) {
        super(LocalizeValue.localizeTODO(text), LocalizeValue.empty(), PhpAnnotationIconGroup.doctrine());
    }

    @Override
    protected boolean isValidForFile(Project project, Editor editor, PsiFile file) {
        if (!DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false;
        }

        PhpClass phpClass = getPhpClassOnValidScope(editor, file);
        if (phpClass == null) {
            return false;
        }

        PhpDocComment docComment = phpClass.getDocComment();
        if (docComment != null) {
            PhpDocCommentAnnotation container = AnnotationUtil.getPhpDocCommentAnnotationContainer(docComment);
            if (container != null && container.getPhpDocBlock(supportedClass()) != null) {
                return false;
            }
        }

        String supportedClass = "\\" + StringUtils.stripStart(supportedClass(), "\\");
        return phpClass.getAttributes(supportedClass).isEmpty();
    }

    @Override
    protected CodeInsightActionHandler getHandler() {
        return (project, editor, file) -> {
            PhpClass phpClass = getPhpClassOnValidScope(editor, file);
            if (phpClass != null) {
                execute(editor, phpClass, file);
            }
        };
    }

    protected abstract void execute(Editor editor, PhpClass phpClass, PsiFile psiFile);

    /**
     * Class supported by this action. if already inside DocBlock dont provide an action
     */
    protected abstract String supportedClass();

    private static @Nullable PhpClass getPhpClassOnValidScope(Editor editor, PsiFile file) {
        if (!(file instanceof PhpFile)) {
            return null;
        }

        int offset = editor.getCaretModel().getOffset();
        if (offset <= 0) {
            return null;
        }

        PsiElement psiElement = file.findElementAt(offset);
        if (psiElement == null || !INSIDE_PHP_CLASS_PATTERN.accepts(psiElement)) {
            return null;
        }

        return PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
    }
}
