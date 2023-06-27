package dev.getelements.elements.service;

import dev.getelements.elements.config.ModuleDefaults;
import dev.getelements.elements.service.formidium.FormidiumConstants;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static dev.getelements.elements.Constants.*;
import static dev.getelements.elements.Constants.NEO_BLOCKCHAIN_PORT;
import static dev.getelements.elements.service.formidium.FormidiumConstants.FORMIDIUM_API_KEY;
import static dev.getelements.elements.service.formidium.FormidiumConstants.FORMIDIUM_API_URL;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ServiceModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(SESSION_TIMEOUT_SECONDS, Long.toString(SECONDS.convert(48, TimeUnit.HOURS)));
        properties.put(MOCK_SESSION_TIMEOUT_SECONDS, Long.toString(SECONDS.convert(1, TimeUnit.HOURS)));
        properties.put(NEO_BLOCKCHAIN_HOST, "http://127.0.0.1");
        properties.put(NEO_BLOCKCHAIN_PORT, "50012");
        properties.put(BSC_RPC_PROVIDER, "https://data-seed-prebsc-1-s1.binance.org:8545");
        properties.setProperty(FORMIDIUM_API_KEY, "");
        properties.setProperty(FORMIDIUM_API_URL, "https://csduat.formidium.com");
        return properties;
    }

}
