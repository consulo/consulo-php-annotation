package de.espend.idea.php.annotation.util;

import consulo.index.io.ID;
import consulo.language.psi.stub.FileBasedIndex;
import de.espend.idea.php.annotation.AnnotationStubIndex;
import de.espend.idea.php.annotation.AnnotationUsageIndex;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public final class PluginUtil {
    private PluginUtil() {
    }

    /**
     * Force reindex of all internal file indexes
     */
    public static void forceReindex() {
        ID<?, ?>[] indexIds = new ID<?, ?>[]{AnnotationStubIndex.KEY, AnnotationUsageIndex.KEY};
        for (ID<?, ?> id : indexIds) {
            FileBasedIndex.getInstance().requestRebuild(id);
        }
    }
}
