package com.namazustudios.socialengine.docserve.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.docserve.StaticPathDocs;
import com.namazustudios.socialengine.docserve.lua.LuaStaticPathDocs;

public class LuaStaticPathDocsModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(StaticPathDocs.class).to(LuaStaticPathDocs.class);
        expose(StaticPathDocs.class);
    }

}
