package de.espend.idea.php.annotation.doctrine.intention;

import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.component.util.Iconable;
import consulo.language.ast.ASTNode;
import consulo.language.editor.intention.HighPriorityAction;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.image.Image;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.icon.PhpAnnotationIconGroup;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import org.jspecify.annotations.Nullable;

/**
 * Intention: "private $id<carpet>;"
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "PhpAnnotations.DoctrineOrmFieldIntention", fileExtensions = "php", categories = "PHP")
public class DoctrineOrmFieldIntention extends PsiElementBaseIntentionAction implements Iconable, HighPriorityAction {
    @Override
    public boolean isAvailable(Project project, Editor editor, PsiElement element) {
        // was plugin.xml <language>PHP</language>
        if (!element.getLanguage().isKindOf(PhpLanguage.INSTANCE)) {
            return false;
        }

        if (!DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false;
        }

        Field context = getFieldContext(element);
        return context != null && !DoctrineUtil.isOrmColumnProperty(context);
    }

    @Override
    public void invoke(Project project, Editor editor, PsiElement psiElement) throws IncorrectOperationException {
        Field context = getFieldContext(psiElement);
        PsiFile containingFile = psiElement.getContainingFile();
        if (context != null && containingFile != null) {
            DoctrineUtil.importOrmUseAliasIfNotExists(context);
            PhpDocUtil.addPropertyOrmDocs(context, editor.getDocument(), containingFile);
        }
    }

    @Override
    public LocalizeValue getText() {
        return LocalizeValue.localizeTODO("Add Doctrine column");
    }

    private static @Nullable Field getFieldContext(PsiElement element) {
        if (element instanceof Field field) {
            return field;
        }

        Field context = null;

        // direct field context
        // public $foo;
        PsiElement firstParent = PsiTreeUtil.findFirstParent(element, psiElement -> {
            ASTNode node = psiElement.getNode();
            return node != null && node.getElementType() == PhpElementTypes.CLASS_FIELDS;
        });

        if (firstParent instanceof PhpPsiElement) {
            context = PsiTreeUtil.getChildOfType(firstParent, Field.class);
        }

        // docblock before field
        // /** <caret> /*
        // public $foo;
        if (context == null) {
            PhpDocComment parentOfType = PsiTreeUtil.getParentOfType(element, PhpDocComment.class);
            if (parentOfType != null) {
                PhpPsiElement nextPsiSibling = parentOfType.getNextPsiSibling();
                if (nextPsiSibling != null && nextPsiSibling.getNode().getElementType() == PhpElementTypes.CLASS_FIELDS) {
                    context = PsiTreeUtil.getChildOfType(nextPsiSibling, Field.class);
                }
            }
        }

        // at the end of the line
        // public $foo;<caret>
        if (element instanceof PsiWhiteSpace) {
            PsiElement prevSibling = element.getPrevSibling();
            if (prevSibling != null && prevSibling.getNode().getElementType() == PhpElementTypes.CLASS_FIELDS) {
                context = PsiTreeUtil.getChildOfType(prevSibling, Field.class);
            }
        }

        return context;
    }

    @Override
    public Image getIcon(int flags) {
        return PhpAnnotationIconGroup.doctrine();
    }
}
