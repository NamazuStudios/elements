package com.namazustudios.socialengine.setup;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.setup.SetupSSHD.*;

public class SetupSSHDModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final var defaults = new Properties();
        defaults.setProperty(SSH_HOST, "");
        defaults.setProperty(SSH_PORT, "2022");
        defaults.setProperty(HOST_KEY, "host_key");
        defaults.setProperty(HOST_CERTIFICATE, "host_certificate");
        defaults.setProperty(AUTHORIZED_KEYS, "authorized_keys");
        return defaults;
   }

}
