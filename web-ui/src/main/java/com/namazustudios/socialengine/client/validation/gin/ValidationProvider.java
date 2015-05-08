package com.namazustudios.socialengine.client.validation.gin;

import javax.inject.Provider;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;


/**
 * Created by patricktwohig on 5/7/15.
 */
public class ValidationProvider implements Provider<Validator> {

    private final ValidatorFactory validatorFactory = Validation.byDefaultProvider()
                                                                .configure()
                                                                .buildValidatorFactory();

    @Override
    public Validator get() {
        return validatorFactory.getValidator();
    }

}
