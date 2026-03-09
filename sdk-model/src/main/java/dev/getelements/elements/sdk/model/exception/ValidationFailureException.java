package dev.getelements.elements.sdk.model.exception;

import jakarta.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Thrown when bean validation fails.
 */
public class ValidationFailureException extends InvalidDataException {

    transient private List<ConstraintViolation<Object>> constraintViolations;

    /**
     * Creates a new instance with the given constraint violations and invalid model.
     * @param constraintViolations the constraint violations
     * @param model the invalid model object
     */
    public ValidationFailureException(final Collection<ConstraintViolation<Object>> constraintViolations, final Object model) {
        super(buildMessage(constraintViolations), model);
        this.constraintViolations = new ArrayList<>(constraintViolations);
    }

    /**
     * Returns the constraint violations that caused this exception.
     * @return the constraint violations
     */
    public List<ConstraintViolation<Object>> getConstraintViolations() {
        return constraintViolations;
    }

    private static String buildMessage(final Collection<ConstraintViolation<Object>> constraintViolations) {
        return "[ " + constraintViolations.stream().map(v -> v.toString()).collect(joining(", ")) + " ]";
    }

}
