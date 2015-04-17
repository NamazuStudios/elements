package com.namazustudios.promotion.client.login;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.namazustudios.promotion.client.guice.RestyModule;

/**
 * Created by patricktwohig on 4/17/15.
 */
@GinModules(RestyModule.class)
public interface LoginWidgetGinjector extends Ginjector {

    LoginPanel getLoginPanel();

}
