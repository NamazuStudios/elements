package com.namazustudios.socialengine;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.ValidationFailureException;
import org.omg.CORBA.DynAnyPackage.Invalid;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Created by patricktwohig on 5/7/15.
 */
public class ValidationHelper {

    @Inject
    private Validator validator;

    /**
     * Validates the given model object.  If the model fails validation, then an instance
     * of ValidationFailureException is thrown.
     *
     * @param model the model
     * @throws ValidationFailureException if validation fails.
     */
    public void validateModel(Object model) throws InvalidDataException, ValidationFailureException {

        if (model == null) {
            throw new InvalidDataException("Received null instance.");
        }

        final Set<ConstraintViolation<Object>> violationSet = validator.validate(model);

        if (!violationSet.isEmpty()) {
            throw new ValidationFailureException(violationSet, model);
        }

    }

}
