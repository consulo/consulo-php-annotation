package de.espend.idea.php.annotation;

import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import consulo.annotation.component.ExtensionImpl;
import consulo.index.io.DataIndexer;
import consulo.index.io.EnumeratorStringDescriptor;
import consulo.index.io.ID;
import consulo.index.io.KeyDescriptor;
import consulo.index.io.data.DataExternalizer;
import consulo.language.psi.PsiFile;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.language.psi.stub.FileBasedIndexExtension;
import consulo.language.psi.stub.FileContent;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationStubIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> KEY = ID.create("espend.php.annotation.classes");

    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @Override
    public ID<String, String> getName() {
        return KEY;
    }

    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            Map<String, String> map = new HashMap<>();
            PsiFile psiFile = inputData.getPsiFile();
            if (!(psiFile instanceof PhpFile phpFile) || !AnnotationUtil.isValidForIndex(inputData)) {
                return map;
            }

            for (PhpNamedElement topLevelElement : phpFile.getTopLevelDefs().values()) {
                if (!(topLevelElement instanceof PhpClass phpClass)) {
                    continue;
                }

                String fqn = StringUtils.removeStart(phpClass.getFQN(), "\\");

                // doctrine has many tests: Doctrine\Tests\Common\Annotations\Fixtures
                // we are on index process, project is not fully loaded here, so filter name based tests
                // e.g. PhpUnitUtil.isTestClass not possible
                if (fqn.contains("\\Tests\\") || fqn.contains("\\Fixtures\\")) {
                    continue;
                }

                String serializedTargets = AnnotationUtil.getSerializedAnnotationTargets(phpClass);
                if (serializedTargets == null) {
                    continue;
                }

                map.put(fqn, serializedTargets);
            }

            return map;
        };
    }

    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return myKeyDescriptor;
    }

    @Override
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return (project, virtualFile) -> virtualFile.getFileType() == PhpFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 3;
    }
}
