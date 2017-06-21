package com.namazustudios.socialengine.client.validation.gin;

import com.google.gwt.inject.client.AbstractGinModule;

import javax.validation.Validator;

/**
 * Created by patricktwohig on 5/7/15.
 */
public class ValidationModule extends AbstractGinModule {

    @Override
    public void configure() {
        binder().bind(Validator.class).toProvider(ValidationProvider.class);
    }

}
