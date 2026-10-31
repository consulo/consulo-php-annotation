package de.espend.idea.php.annotation;

import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
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
import de.espend.idea.php.annotation.util.PhpDocTagAnnotationVisitorUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class AnnotationUsageIndex extends FileBasedIndexExtension<String, Set<String>> {
    public static final ID<String, Set<String>> KEY = ID.create("espend.php.annotation.usage");

    private static final DataExternalizer<Set<String>> EXTERNALIZER = new StringSetDataExternalizer();

    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @Override
    public ID<String, Set<String>> getName() {
        return KEY;
    }

    @Override
    public DataIndexer<String, Set<String>, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Set<String>> map = new HashMap<>();
            PsiFile psiFile = inputData.getPsiFile();
            if (!(psiFile instanceof PhpFile phpFile) || !AnnotationUtil.isValidForIndex(inputData)) {
                return map;
            }

            PhpDocTagAnnotationVisitorUtil.visitElement(phpFile, pair -> {
                map.put(pair.getFirst(), new HashSet<>());
                return true;
            });

            return map;
        };
    }

    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return myKeyDescriptor;
    }

    @Override
    public DataExternalizer<Set<String>> getValueExternalizer() {
        return EXTERNALIZER;
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
        return 1;
    }

    private static class StringSetDataExternalizer implements DataExternalizer<Set<String>> {
        @Override
        public synchronized void save(DataOutput out, Set<String> value) throws IOException {
            out.writeInt(value.size());
            for (String item : value) {
                EnumeratorStringDescriptor.INSTANCE.save(out, item);
            }
        }

        @Override
        public synchronized Set<String> read(DataInput in) throws IOException {
            Set<String> set = new HashSet<>();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                set.add(EnumeratorStringDescriptor.INSTANCE.read(in));
            }
            return set;
        }
    }
}
