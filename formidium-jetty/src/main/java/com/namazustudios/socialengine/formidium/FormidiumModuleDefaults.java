package com.namazustudios.socialengine.formidium;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.service.formidium.FormidiumConstants.*;

public class FormidiumModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final var properties = new Properties();
        properties.setProperty(FORMIDIUM_API_KEY, "");
        properties.setProperty(FORMIDIUM_API_URL, "https://csduat.formidium.com");
        properties.setProperty(FORMIDIUM_CONTEXT_ROOT, "formidium");
        return properties;
    }

}
