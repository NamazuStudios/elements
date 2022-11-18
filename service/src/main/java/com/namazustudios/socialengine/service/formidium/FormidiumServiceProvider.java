package com.namazustudios.socialengine.service.formidium;

import com.namazustudios.socialengine.service.Services;

import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class FormidiumServiceProvider implements Provider<FormidiumService> {

    @Override
    public FormidiumService get() {
        // TODO Bind to Formidium Service
        return forbidden(FormidiumService.class);
    }

}
