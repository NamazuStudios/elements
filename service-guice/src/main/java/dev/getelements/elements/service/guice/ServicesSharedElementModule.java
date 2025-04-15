package dev.getelements.elements.service.guice;

import com.google.inject.Scope;
import dev.getelements.elements.sdk.guice.SharedElementModule;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class ServicesSharedElementModule extends SharedElementModule {

    public ServicesSharedElementModule() {
        super("dev.getelements.elements.sdk.service");
    }

    @Override
    protected void configureElement(Scope scope) {
        install(new ServicesModule(scope));
    }

}
