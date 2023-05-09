package dev.getelements.elements.formidium;

import dev.getelements.elements.config.ModuleDefaults;

import java.util.Properties;

import static dev.getelements.elements.service.formidium.FormidiumConstants.*;

public class FormidiumModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final var properties = new Properties();
        properties.setProperty(FORMIDIUM_API_KEY, "");
        properties.setProperty(FORMIDIUM_API_URL, "https://csduat.formidium.com");
        properties.setProperty(FormidiumAppProvider.FORMIDIUM_CONTEXT_ROOT, "formidium");
        return properties;
    }

}
