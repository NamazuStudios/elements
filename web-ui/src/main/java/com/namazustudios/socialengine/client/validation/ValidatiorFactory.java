package com.namazustudios.socialengine.client.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.validation.client.AbstractGwtValidatorFactory;
import com.google.gwt.validation.client.GwtValidation;
import com.google.gwt.validation.client.impl.AbstractGwtValidator;
import com.namazustudios.socialengine.model.ShortLink;
import com.namazustudios.socialengine.model.User;

import javax.validation.Validator;

/**
 * Created by patricktwohig on 5/7/15.
 */
public class ValidatiorFactory extends AbstractGwtValidatorFactory {

    @GwtValidation({User.class, ShortLink.class})
    public interface GwtValidator extends Validator {}

    @Override
    public AbstractGwtValidator createValidator() {
        return GWT.create(GwtValidator.class);
    }

}
