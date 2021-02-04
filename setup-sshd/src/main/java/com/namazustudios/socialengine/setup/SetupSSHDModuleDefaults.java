package com.namazustudios.socialengine.setup;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.setup.SetupSSHD.SSH_PORT;

public class SetupSSHDModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final var defaults = new Properties();
        defaults.setProperty(SSH_PORT, "2022");
        return defaults;
   }

}
