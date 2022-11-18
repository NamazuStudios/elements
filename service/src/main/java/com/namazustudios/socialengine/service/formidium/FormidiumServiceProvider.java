package com.namazustudios.socialengine.service.formidium;

import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.unimplemented;

public class FormidiumServiceProvider implements Provider<FormidiumService> {

    @Override
    public FormidiumService get() {
        return unimplemented(FormidiumService.class);
    }

}
