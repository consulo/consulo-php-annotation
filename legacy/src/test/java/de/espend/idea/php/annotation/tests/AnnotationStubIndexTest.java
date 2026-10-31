package de.espend.idea.php.annotation.tests;

import de.espend.idea.php.annotation.AnnotationStubIndex;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import de.espend.idea.php.annotation.util.AnnotationUtil;

import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationStubIndexTest extends AnnotationLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
        myFixture.copyFileToProject("classes_targets.php");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/fixtures";
    }

    public void testThatAnnotationClassIsInIndex() {
        assertIndexContains(AnnotationStubIndex.KEY, "My\\Annotations\\Route");
        assertIndexContains(AnnotationStubIndex.KEY, "My\\Annotations\\Foo\\RouteBar");
        assertIndexContains(AnnotationStubIndex.KEY, "My\\Annotations\\Foo\\RouteFoo");
    }

    public void testThatAnnotationTargetsAreStoredInIndex() {
        assertIndexContainsKeyWithValue(AnnotationStubIndex.KEY, "My\\Annotations\\PropertyOnly", value ->
            AnnotationUtil.getAnnotationTargetsFromSerializedValue(value).contains(AnnotationTarget.PROPERTY)
        );

        assertIndexContainsKeyWithValue(AnnotationStubIndex.KEY, "My\\Annotations\\MethodAndAll", value -> {
            List<AnnotationTarget> targets = AnnotationUtil.getAnnotationTargetsFromSerializedValue(value);
            return targets.contains(AnnotationTarget.METHOD) && targets.contains(AnnotationTarget.ALL);
        });
    }
}
