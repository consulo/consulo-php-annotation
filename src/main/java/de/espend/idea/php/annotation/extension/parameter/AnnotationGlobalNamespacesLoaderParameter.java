package de.espend.idea.php.annotation.extension.parameter;

import consulo.project.Project;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationGlobalNamespacesLoaderParameter {
    private final Project project;

    public Project getProject() {
        return project;
    }

    public AnnotationGlobalNamespacesLoaderParameter(Project project) {
        this.project = project;
    }
}
