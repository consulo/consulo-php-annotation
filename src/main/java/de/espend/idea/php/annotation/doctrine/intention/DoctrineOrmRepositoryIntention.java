package de.espend.idea.php.annotation.doctrine.intention;

import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.codeEditor.Editor;
import consulo.component.util.Iconable;
import consulo.fileEditor.FileEditorManager;
import consulo.fileTemplate.FileTemplate;
import consulo.fileTemplate.FileTemplateManager;
import consulo.fileTemplate.FileTemplateUtil;
import consulo.language.ast.ASTNode;
import consulo.language.editor.hint.HintManager;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.intention.HighPriorityAction;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.navigation.OpenFileDescriptorFactory;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.icon.PhpAnnotationIconGroup;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import de.espend.idea.php.annotation.util.PhpPsiAttributesUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "PhpAnnotations.DoctrineOrmRepositoryIntention", fileExtensions = "php", categories = "PHP")
public class DoctrineOrmRepositoryIntention extends PsiElementBaseIntentionAction implements LocalQuickFix, Iconable, HighPriorityAction {
    @Override
    public boolean isAvailable(Project project, Editor editor, PsiElement element) {
        // was plugin.xml <language>PHP</language>
        if (!element.getLanguage().isKindOf(PhpLanguage.INSTANCE)) {
            return false;
        }

        if (!DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false;
        }

        PhpClass phpClass = getScopedPhpClass(element);
        return phpClass != null && DoctrineUtil.hasCreateRepositoryClassSupport(phpClass);
    }

    @Override
    public void invoke(Project project, Editor editor, PsiElement element) throws IncorrectOperationException {
        doInvoke(project, editor, element);
    }

    private void doInvoke(Project project, @Nullable Editor editor, PsiElement element) {
        PhpClass phpClass = getScopedPhpClass(element);
        if (phpClass == null) {
            return;
        }

        Editor currentEditor = editor != null ? editor : FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (currentEditor == null) {
            return;
        }

        // skip for preview
        PsiFile phpClassFile = phpClass.getContainingFile();
        PsiDirectory containingDirectory = phpClassFile != null ? phpClassFile.getContainingDirectory() : null;
        if (containingDirectory == null) {
            return;
        }

        String fqn = phpClass.getFQN();

        String sameNamespace = fqn + "Repository";
        if (PhpElementsUtil.getClass(project, sameNamespace) != null) {
            insertRepositoryClass(currentEditor, element, phpClass, sameNamespace);
            return;
        }

        String[] split = StringUtils.split(StringUtils.stripStart(fqn, "\\"), "\\");

        // Foo\Entity\Foobar => Foo\Repository\FoobarRepository
        if (split.length > 2) {
            String repoNamespace = "\\" + String.join("\\", Arrays.copyOfRange(split, 0, split.length - 2))
                + "\\Repository\\" + split[split.length - 1] + "Repository";

            if (PhpElementsUtil.getClass(project, repoNamespace) != null) {
                insertRepositoryClass(currentEditor, element, phpClass, repoNamespace);
                return;
            }
        }

        // Foo\Entity\Foo\Foobar => Foo\Repository\Foo\FoobarRepository
        int entityIndex = fqn.lastIndexOf("\\Entity\\");
        if (entityIndex > 0) {
            String repoNamespace = new StringBuilder(fqn)
                .replace(entityIndex, entityIndex + "\\Entity\\".length(), "\\Repository\\")
                .append("Repository")
                .toString();

            if (PhpElementsUtil.getClass(project, repoNamespace) != null) {
                insertRepositoryClass(currentEditor, element, phpClass, repoNamespace);
                return;
            }
        }

        // Foo\Entity\Foobar => Foo\Entity\Repository\FoobarRepository
        int namespaceIndex = fqn.lastIndexOf("\\");
        if (namespaceIndex > 0) {
            String repoNamespace = new StringBuilder(fqn).insert(namespaceIndex, "\\Repository") + "Repository";
            if (PhpElementsUtil.getClass(project, repoNamespace) != null) {
                insertRepositoryClass(currentEditor, element, phpClass, repoNamespace);
                return;
            }
        }

        String fileName = phpClass.getName() + "Repository.php";

        PsiDirectory repositoryDir = null;
        PsiDirectory parentDirectory = containingDirectory.getParentDirectory();
        if (parentDirectory != null) {
            repositoryDir = parentDirectory.findSubdirectory("Repository");
            if (repositoryDir == null) {
                repositoryDir = parentDirectory.createSubdirectory("Repository");
            }
        }

        boolean headless = Application.get().isHeadlessEnvironment();
        if (repositoryDir == null) {
            if (!headless) {
                HintManager.getInstance().showErrorHint(currentEditor, "Repository directory structure can not be created");
            }
            return;
        }

        if (repositoryDir.findFile(fileName) != null) {
            if (!headless) {
                HintManager.getInstance().showErrorHint(currentEditor, "Repository file already exists");
            }
            return;
        }

        String templateName = PhpElementsUtil.getClass(project, "\\Doctrine\\Bundle\\DoctrineBundle\\Repository\\ServiceEntityRepository") != null
            ? "Doctrine Entity ServiceRepository"
            : "Doctrine Entity Repository";

        FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(project);
        FileTemplate fileTemplate = fileTemplateManager.getInternalTemplate(templateName);
        Map<String, Object> properties = fileTemplateManager.getDefaultVariables();

        // Foo\Entity\Foobar => Foo\Repository\FoobarRepository
        String repoClass = "\\" + String.join("\\", Arrays.copyOfRange(split, 0, Math.max(0, split.length - 2)))
            + "\\Repository\\" + split[split.length - 1] + "Repository";

        properties.put("NAMESPACE", StringUtils.stripStart(repoClass.substring(0, repoClass.lastIndexOf("\\")), "\\"));
        properties.put("NAME", phpClass.getName() + "Repository");
        properties.put("ENTITY_NAMESPACE", DoctrineUtil.trimBlackSlashes(phpClass.getNamespaceName()));
        properties.put("ENTITY_NAME", phpClass.getName());

        PsiElement newElement;
        try {
            newElement = FileTemplateUtil.createFromTemplate(fileTemplate, fileName, properties, repositoryDir);
        }
        catch (Exception e) {
            return;
        }

        insertRepositoryClass(currentEditor, element, phpClass, repoClass);

        PsiFile newFile = newElement.getContainingFile();
        VirtualFile virtualFile = newFile != null ? newFile.getVirtualFile() : null;
        if (virtualFile != null) {
            OpenFileDescriptorFactory.getInstance(project).builder(virtualFile).offset(0).build().navigate(true);
        }
    }

    /**
     * Scope resolve for PhpClass:
     * "@ORM\Entity" or inside PhpClass
     */
    private static @Nullable PhpClass getScopedPhpClass(PsiElement element) {
        // inside "@ORM\Entity"
        PsiElement parent = element.getParent();
        if (parent == null) {
            return null;
        }

        // inside "@ORM\Entity(<caret>)"
        ASTNode parentNode = parent.getNode();
        if (parentNode != null && parentNode.getElementType() == PhpDocElementTypes.phpDocAttributeList) {
            parent = parent.getParent();
        }

        if (parent instanceof PhpDocTag phpDocTag) {
            PhpDocTagAnnotation phpDocAnnotationContainer = AnnotationUtil.getPhpDocAnnotationContainer(phpDocTag);
            if (phpDocAnnotationContainer != null) {
                PhpClass phpClass = phpDocAnnotationContainer.getPhpClass();
                if ("Doctrine\\ORM\\Mapping\\Entity".equals(phpClass.getPresentableFQN())) {
                    if (phpDocTag.getParent() instanceof PhpDocComment docTag && docTag.getNextPsiSibling() instanceof PhpClass nextClass) {
                        return nextClass;
                    }
                }
            }

            return null;
        }

        // and finally check PhpClass class scope
        return PsiTreeUtil.getParentOfType(element, PhpClass.class);
    }

    @Override
    public LocalizeValue getName() {
        return getText();
    }

    @Override
    public void applyFix(Project project, ProblemDescriptor descriptor) {
        PsiElement psiElement = descriptor.getPsiElement();
        if (psiElement == null) {
            return;
        }

        PsiFile containingFile = psiElement.getContainingFile();
        if (containingFile == null) {
            return;
        }

        doInvoke(project, null, containingFile);
    }

    @Override
    public LocalizeValue getText() {
        return LocalizeValue.localizeTODO("Add Doctrine repository");
    }

    @Override
    public Image getIcon(int flags) {
        return PhpAnnotationIconGroup.doctrine();
    }

    private static void insertRepositoryClass(Editor editor, PsiElement element, PhpClass phpClass, String repoClass) {
        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(element);
        if (scopeForUseOperator == null) {
            return;
        }

        PhpElementsUtil.insertUseIfNecessary(scopeForUseOperator, repoClass, null);
        PsiDocumentManager.getInstance(element.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());

        String phpDocTagName = PhpDocUtil.getQualifiedName(phpClass, repoClass);

        PhpDocTagAnnotation ormEntityPhpDocBlock = DoctrineUtil.getOrmEntityPhpDocBlock(phpClass);
        if (ormEntityPhpDocBlock != null) {
            AnnotationUtil.insertNamedArgumentForAnnotation(editor, ormEntityPhpDocBlock.getPhpDocTag(), "repositoryClass", phpDocTagName + "::class");
            return;
        }

        Collection<PhpAttribute> attributes = phpClass.getAttributes("\\Doctrine\\ORM\\Mapping\\Entity");
        if (!attributes.isEmpty()) {
            PhpPsiAttributesUtil.insertNamedArgumentForAttribute(editor, attributes.iterator().next(), "repositoryClass", phpDocTagName + "::class");
        }
    }
}
