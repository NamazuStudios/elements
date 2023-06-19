package dev.getelements.elements.codeserve;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.rt.ApplicationBootstrapper;
import dev.getelements.elements.rt.ApplicationBootstrapper.BootstrapResources;
import dev.getelements.elements.rt.id.ApplicationId;
import dev.getelements.elements.rt.lua.LuaBootstrapResources;

import java.util.function.Function;

public class LuaBootstrapResourcesModule extends PrivateModule {

    @Override
    protected void configure() {

        // Right now there is only one bootstrapper, the Lua bootstrapper.  For the sake of simplicity
        // we just return the instance as requested, but this could be expanded to more languages or
        // frameworks in the future.

        final var tl = new TypeLiteral<Function<ApplicationId, BootstrapResources>>(){};
        bind(tl).toInstance(a -> new LuaBootstrapResources());
        expose(tl);

    }

}
