package de.espend.idea.php.annotation.reference;

import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocToken;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementResolveResult;
import consulo.language.psi.PsiPolyVariantReferenceBase;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.PsiReferenceRegistrar;
import consulo.language.psi.ResolveResult;
import consulo.language.util.ProcessingContext;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class DocTagNameAnnotationReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        /*
         * Our main reference provider to attach DocBlocTag to their use declaration
         * This one resolve the "Optimize Usage" issues
         *
         * "@Template()", "@ORM\PostPersist()"
         */
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PhpDocTag.class), new PsiReferenceProvider() {
            @Override
            public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
                if (!(element instanceof PhpDocTag phpDocTag)) {
                    return PsiReference.EMPTY_ARRAY;
                }

                return new PsiReference[]{new PhpDocTagReference(phpDocTag)};
            }
        });

        /*
         * Collects static identifier elements on the first element and search them inside the use statements or global namespace
         *
         * - @Foo(F<caret>OO::BAR)
         * - @Foo(Fo<caret>o\FOO::BAR)
         */
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PhpDocToken.class), new PsiReferenceProvider() {
            @Override
            public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
                if (element.getNode().getElementType() != PhpDocTokenTypes.DOC_IDENTIFIER) {
                    return PsiReference.EMPTY_ARRAY;
                }

                PsiElement prevSibling = element.getPrevSibling();
                if (prevSibling == null
                    || prevSibling.getNode().getElementType() == PhpDocTokenTypes.DOC_NAMESPACE
                    || PhpDocUtil.isDocStaticElement(prevSibling)) {
                    return PsiReference.EMPTY_ARRAY;
                }

                // We must be at first namespace part: "F<caret>oo\Bar::class"
                if (PhpDocUtil.getNamespaceForDocIdentifierAtStart(element) == null) {
                    return PsiReference.EMPTY_ARRAY;
                }

                PsiElement docTag = PhpPsiUtil.getParentByCondition(element, true, PhpDocTag.INSTANCEOF, null);
                if (docTag == null) {
                    return PsiReference.EMPTY_ARRAY;
                }

                // Find any import which is related here: "use Foo" => "F<caret>oo\Bar::class"
                String fqn = AnnotationUtil.getClassFromDocIdentifierAsString(element);
                if (fqn == null) {
                    return PsiReference.EMPTY_ARRAY;
                }

                return new PsiReference[]{new PhpDocIdentifierReference(element, fqn)};
            }
        });
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    private static class PhpDocTagReference extends PsiPolyVariantReferenceBase<PhpDocTag> {
        PhpDocTagReference(PhpDocTag element) {
            super(element);
        }

        @Override
        public boolean isReferenceTo(PsiElement element) {
            // eg for "Optimize Imports"
            // attach reference to @Template()
            // reference can also point to a namespace e.g. @Annotation\Exclude()
            // use Doctrine\ORM\Mapping as "ORM";
            if (element instanceof PhpUse phpUse) {
                if (getDocBlockName().equals(phpUse.getName())) {
                    return true;
                }
            }

            // class "Zend\Form\Annotation\Exclude" imported via namespace and has "subclass" annotation
            // use Zend\Form\Annotation;
            // @Annotation\Exclude
            if (element instanceof PhpNamespace phpNamespace) {
                PhpClass phpClass = AnnotationUtil.getAnnotationReference(getElement());
                return phpClass != null && phpClass.getFQN().startsWith(phpNamespace.getFQN());
            }

            // direct class match
            // Zend\Form\Annotation => @Annotation
            if (element instanceof PhpClass otherClass) {
                PhpClass phpClass = AnnotationUtil.getAnnotationReference(getElement());
                return phpClass != null && phpClass.getFQN().equals(otherClass.getFQN());
            }

            return false;
        }

        /**
         * We need to strip @ char before DocTag @Test, @Test\Foo
         *
         * @return TextRange of DocTag without @ char
         */
        @Override
        public TextRange getRangeInElement() {
            String tagName = getElement().getName();
            int rangeStart = 0;
            int rangeEnd = tagName.length();

            // remove DocTag "@" char
            // it should always be true, check for security reason
            if (tagName.startsWith("@")) {
                rangeStart = 1;
                tagName = tagName.substring(1);
            }

            // "@ORM\PostPersist()"
            // only on alias and namespace use main ns
            if (tagName.contains("\\")) {
                rangeEnd = tagName.indexOf("\\") + rangeStart;
            }

            return new TextRange(rangeStart, rangeEnd);
        }

        @Override
        public ResolveResult[] multiResolve(boolean incompleteCode) {
            PhpClass phpClass = AnnotationUtil.getAnnotationReference(getElement());
            if (phpClass == null) {
                return ResolveResult.EMPTY_ARRAY;
            }

            return new ResolveResult[]{new PsiElementResolveResult(phpClass)};
        }

        @Override
        public Object[] getVariants() {
            return new Object[0];
        }

        /**
         * Get the class alias
         * <p>
         * "@Template()"
         * "@ORM\PostPersist()"
         */
        private String getDocBlockName() {
            String name = getElement().getName();
            if (name.startsWith("@")) {
                name = name.substring(1);
            }

            int index = name.indexOf("\\");
            if (index >= 0) {
                name = name.substring(0, index);
            }

            return name;
        }
    }

    /**
     * Adds support for references of "@Foobar(name=Fo<caret>oBar::Const)"
     */
    private static class PhpDocIdentifierReference extends PsiReferenceBase<PsiElement> {
        private final String myFqn;

        PhpDocIdentifierReference(PsiElement element, String fqn) {
            super(element);
            myFqn = fqn;
        }

        @Override
        public TextRange getRangeInElement() {
            return TextRange.create(0, getElement().getTextLength());
        }

        @Override
        public @Nullable PsiElement resolve() {
            return null;
        }

        /**
         * Attach element identify name to class of "use" usage
         *
         * @param psiElement PhpClass used in "use" statement
         */
        @Override
        public boolean isReferenceTo(PsiElement psiElement) {
            if (!(psiElement instanceof PhpNamedElement phpNamedElement)) {
                return false;
            }

            String text = getElement().getText();
            return StringUtils.isNotBlank(text)
                && AnnotationUtil.getUseImportMap(getElement()).containsKey(text)
                && StringUtils.stripStart(phpNamedElement.getFQN(), "\\").equalsIgnoreCase(StringUtils.stripStart(myFqn, "\\"));
        }

        @Override
        public Object[] getVariants() {
            return new Object[0];
        }
    }
}
