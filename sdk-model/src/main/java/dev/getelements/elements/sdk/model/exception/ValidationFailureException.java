package dev.getelements.elements.sdk.model.exception;

import jakarta.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Created by patricktwohig on 5/7/15.
 */
public class ValidationFailureException extends InvalidDataException {

    transient private List<ConstraintViolation<Object>> constraintViolations;

    public ValidationFailureException(final Collection<ConstraintViolation<Object>> constraintViolations, final Object model) {
        super(buildMessage(constraintViolations), model);
        this.constraintViolations = new ArrayList<>(constraintViolations);
    }

    public List<ConstraintViolation<Object>> getConstraintViolations() {
        return constraintViolations;
    }

    private static String buildMessage(final Collection<ConstraintViolation<Object>> constraintViolations) {
        return "[ " + constraintViolations.stream().map(v -> v.toString()).collect(joining(", ")) + " ]";
    }

}
