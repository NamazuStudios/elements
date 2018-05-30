package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.lua.LuaResource;
import com.namazustudios.socialengine.rt.lua.persist.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;

import java.util.function.Supplier;

import static com.namazustudios.socialengine.rt.lua.Constants.ATTRIBUTES_MODULE;

public class AttributesBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(JavaObjectBuiltin.class);

    private static final String ATTRIBUTES = "attributes";

    private final Supplier<Attributes> attributesSupplier;

    public AttributesBuiltin(final Supplier<Attributes> attributesSupplier) {
        this.attributesSupplier = attributesSupplier;
    }

    @Override
    public Module getModuleNamed(final String name) {
        return new Module() {

            @Override
            public String getChunkName() {
                return ATTRIBUTES_MODULE;
            }

            @Override
            public boolean exists() {
                return ATTRIBUTES_MODULE.equals(name);
            }

        };
    }

    @Override
    public JavaFunction getLoader() {
        return luaState -> {
            final String name = luaState.checkString(1);
            final Module module = luaState.checkJavaObject(2, Module.class);
            logger.debug("Loading module {} - {}", name, module.getChunkName());
            luaState.setTop(0);
            luaState.pushJavaObject(attributesSupplier.get());
            return 1;
        };
    }

}
