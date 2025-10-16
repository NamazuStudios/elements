package dev.getelements.elements.rt.exception;

import dev.getelements.elements.sdk.model.exception.InternalException;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.List.copyOf;

public class ApplicationDeploymentException extends InternalException {

    private final List<String> logs;

    private final List<Throwable> causes;

    public ApplicationDeploymentException(final List<? extends Throwable> causes, final List<String> logs) {
        super(String.join(",", logs), causes == null || causes.isEmpty() ? null : causes.getFirst());
        this.logs = List.copyOf(logs);
        this.causes = causes == null ? emptyList() : copyOf(causes);
    }

    public List<String> getLogs() {
        return logs;
    }

    public List<Throwable> getCauses() {
        return causes;
    }

}
