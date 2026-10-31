package de.espend.idea.php.annotation.tests.util.ide;

import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import de.espend.idea.php.annotation.util.ide.PluginErrorReporterSubmitter;

public class PluginErrorReporterSubmitterTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void testCanBeCreated() {
        PluginErrorReporterSubmitter submitter = new PluginErrorReporterSubmitter();

        assertEquals("Report to espend.de", submitter.getReportActionText());
    }
}
