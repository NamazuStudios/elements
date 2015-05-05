package com.namazustudios.socialengine.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.gin.DefaultModule;
import com.namazustudios.socialengine.client.rest.RestyModule;
import com.namazustudios.socialengine.client.place.NameTokens;

/**
 * Created by patricktwohig on 4/28/15.
 */
public class ControlPanelMain extends AbstractGinModule {

    @Override
    protected void configure() {

        install(new DefaultModule());
        install(new ControlPanelPresenters());
        install(new RestyModule());

        bindConstant().annotatedWith(DefaultPlace.class).to(NameTokens.LOGIN);
        bindConstant().annotatedWith(ErrorPlace.class).to(NameTokens.LOGIN);
        bindConstant().annotatedWith(UnauthorizedPlace.class).to(NameTokens.LOGIN);

    }

}
