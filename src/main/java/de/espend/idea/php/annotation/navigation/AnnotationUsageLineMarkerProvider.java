package de.espend.idea.php.annotation.navigation;

import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.gutter.LineMarkerProvider;
import consulo.language.editor.ui.navigation.NavigationGutterIconBuilder;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.FileBasedIndex;
import de.espend.idea.php.annotation.AnnotationUsageIndex;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationUsageLineMarkerProvider implements LineMarkerProvider {
    /**
     * class "Foo" extends
     */
    private static final ElementPattern<PsiElement> CLASS_NAME_PATTERN = PlatformPatterns
        .psiElement(PhpTokenTypes.IDENTIFIER)
        .afterLeafSkipping(
            PlatformPatterns.psiElement(PsiWhiteSpace.class),
            PlatformPatterns.psiElement(PhpTokenTypes.kwCLASS)
        )
        .withParent(PhpClass.class)
        .withLanguage(PhpLanguage.INSTANCE);

    @RequiredReadAction
    @Override
    public @Nullable LineMarkerInfo getLineMarkerInfo(PsiElement psiElement) {
        return null;
    }

    @RequiredReadAction
    @Override
    public void collectSlowLineMarkers(java.util.List<PsiElement> psiElements, Collection<LineMarkerInfo> results) {
        for (PsiElement psiElement : psiElements) {
            if (!CLASS_NAME_PATTERN.accepts(psiElement)) {
                continue;
            }

            if (!(psiElement.getContext() instanceof PhpClass phpClass)) {
                continue;
            }

            boolean isAnnotationOrAttribute = AnnotationUtil.isAnnotationClass(phpClass) || !phpClass.getAttributes("\\Attribute").isEmpty();
            if (!isAnnotationOrAttribute) {
                continue;
            }

            String fqn = StringUtils.stripStart(phpClass.getFQN(), "\\");

            // find one index annotation class and stop processing on first match
            boolean[] processed = {false};

            FileBasedIndex.getInstance().getFilesWithKey(
                AnnotationUsageIndex.KEY,
                Set.of(fqn),
                virtualFile -> {
                    processed[0] = true;

                    // stop on first match
                    return false;
                },
                GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(psiElement.getProject()), PhpFileType.INSTANCE)
            );

            // we found at least one target to provide lazy target linemarker
            if (processed[0]) {
                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(PhpIcons.IMPLEMENTS)
                    .setTargets(() -> AnnotationUtil.getImplementationsForAnnotation(psiElement.getProject(), fqn))
                    .setTooltipText("Navigate to implementations");

                results.add(builder.createLineMarkerInfo(psiElement));
            }
        }
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
