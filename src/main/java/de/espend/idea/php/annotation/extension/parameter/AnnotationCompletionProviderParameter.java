package de.espend.idea.php.annotation.extension.parameter;

import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.util.ProcessingContext;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationCompletionProviderParameter {
    final private CompletionParameters parameters;

    final private ProcessingContext context;

    final private CompletionResultSet result;

    public AnnotationCompletionProviderParameter(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
        this.parameters = parameters;
        this.context = context;
        this.result = result;
    }

    public CompletionParameters getParameters() {
        return parameters;
    }

    public ProcessingContext getContext() {
        return context;
    }

    public CompletionResultSet getResult() {
        return result;
    }
}
