package de.espend.idea.php.annotation.inspection;

import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import consulo.util.lang.Pair;
import de.espend.idea.php.annotation.inspection.visitor.PhpDocTagWithUsePsiElementVisitor;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Inspection DocTags and their imports
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationMissingUseInspection extends AnnotationLocalInspectionTool {
    public static final String MESSAGE = "[Annotations] Missing import";

    public AnnotationMissingUseInspection() {
        super("AnnotationMissingUseInspection", "[Annotations] Missing import");
    }

    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpDocTagWithUsePsiElementVisitor(holder, this::visitAnnotationDocTag);
    }

    private void visitAnnotationDocTag(
        PhpDocTag phpDocTag,
        ProblemsHolder holder,
        AnnotationInspectionUtil.LazyNamespaceImportResolver lazyNamespaceImportResolver
    ) {
        // Target for our inspection is DocTag name: @Foobar() => Foobar
        // This prevent highlighting the complete DocTag
        PsiElement firstChild = phpDocTag.getFirstChild();
        if (firstChild == null || firstChild.getNode().getElementType() != PhpDocElementTypes.DOC_TAG_NAME) {
            return;
        }

        String tagName = StringUtils.stripStart(phpDocTag.getName(), "@");

        // ignore "@\Foo" absolute FQN ones
        if (tagName.startsWith("\\")) {
            return;
        }

        String[] split = tagName.split("\\\\");

        Map<String, String> useImportMap = lazyNamespaceImportResolver.getImports();
        if (useImportMap.containsKey(split[0])) {
            return;
        }

        PhpClass annotationReference = AnnotationUtil.getAnnotationReference(phpDocTag);
        if (annotationReference != null) {
            return;
        }

        Map<String, String> phpClasses = AnnotationUtil.getPossibleImportClasses(phpDocTag);
        if (!phpClasses.isEmpty()) {
            Collection<Pair<String, @Nullable String>> collect = new ArrayList<>();
            for (Map.Entry<String, String> entry : phpClasses.entrySet()) {
                collect.add(Pair.create(entry.getKey(), entry.getValue()));
            }

            holder.newProblem(LocalizeValue.of(MESSAGE))
                .range(firstChild)
                .highlightType(ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
                .withFixes(new ImportUseForAnnotationQuickFix(phpDocTag, collect))
                .create();
        }
    }
}
