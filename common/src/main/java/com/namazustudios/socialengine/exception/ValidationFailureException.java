package com.namazustudios.socialengine.exception;

import javax.validation.ConstraintViolation;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.*;

/**
 * Created by patricktwohig on 5/7/15.
 */
public class ValidationFailureException extends InvalidDataException {

    transient private List<ConstraintViolation<Object>> constraintViolations;

    public ValidationFailureException() {
        constraintViolations = Collections.emptyList();
    }

    public ValidationFailureException(final Collection<ConstraintViolation<Object>> constraintViolations, final Object model) {
        super("Validation failure.", model);
        this.constraintViolations = new ArrayList<>(constraintViolations);
    }

    public List<ConstraintViolation<Object>> getConstraintViolations() {
        return constraintViolations;
    }

}
