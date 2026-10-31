package de.espend.idea.php.annotation.doctrine.inspection;

import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.inspection.AnnotationLocalInspectionTool;
import de.espend.idea.php.annotation.util.AnnotationUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class RepositoryClassInspection extends AnnotationLocalInspectionTool {
    public static final String MESSAGE = "[Annotations] Missing repository class";

    public RepositoryClassInspection() {
        super("RepositoryClassInspection", "[Annotations] Missing repository class");
    }

    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        if (!DoctrineUtil.isDoctrineOrmInVendor(holder.getProject())) {
            return super.buildVisitor(holder, isOnTheFly);
        }

        return new MyAnnotationPropertyPsiElementVisitor("Doctrine\\ORM\\Mapping\\Entity") {
            @Override
            protected void visitAnnotationProperty(PhpDocTag phpDocTag) {
                StringLiteralExpression repositoryClass = AnnotationUtil.getPropertyValueAsPsiElement(phpDocTag, "repositoryClass");
                if (repositoryClass == null) {
                    return;
                }

                if (!DoctrineUtil.repositoryClassExists(phpDocTag)) {
                    holder.newProblem(LocalizeValue.of(MESSAGE))
                        .range(repositoryClass)
                        .withFixes(new DoctrineOrmRepositoryIntention())
                        .create();
                }
            }
        };
    }

    private abstract static class MyAnnotationPropertyPsiElementVisitor extends PsiElementVisitor {
        private final String myClassName;

        MyAnnotationPropertyPsiElementVisitor(String className) {
            myClassName = className;
        }

        @Override
        public void visitElement(PsiElement psiElement) {
            if (psiElement instanceof PhpDocTag phpDocTag
                && !AnnotationUtil.isBlockedAnnotationTag(phpDocTag.getName())
                && AnnotationUtil.isAnnotationPhpDocTag(phpDocTag)) {
                PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
                if (phpClass != null && myClassName.equals(phpClass.getPresentableFQN())) {
                    visitAnnotationProperty(phpDocTag);
                }
            }

            super.visitElement(psiElement);
        }

        protected abstract void visitAnnotationProperty(PhpDocTag phpDocTag);
    }
}
