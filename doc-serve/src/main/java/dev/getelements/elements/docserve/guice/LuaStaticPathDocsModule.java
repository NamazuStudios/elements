package dev.getelements.elements.docserve.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.docserve.StaticPathDocs;
import dev.getelements.elements.docserve.lua.LuaStaticPathDocs;

public class LuaStaticPathDocsModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(StaticPathDocs.class).to(LuaStaticPathDocs.class);
        expose(StaticPathDocs.class);
    }

}
