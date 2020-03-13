package com.namazustudios.socialengine.rest;

import com.google.inject.Module;
import com.google.inject.Stage;

import java.util.ArrayList;
import java.util.List;

import static com.namazustudios.socialengine.rest.RestAPIMain.*;

public class RestAPIMainBuilder {

    private String bind = DEFAULT_BIND_ADDRESS;

    private int port = DEFAULT_PORT;

    private Stage stage = DEFAULT_STAGE;

    private String apiContext = DEFAULT_API_CONTEXT;

    private List<Module> additionalModules = new ArrayList<>();

    public RestAPIMainBuilder withBindAddress(final String bindAddress) {
        if (bindAddress == null) throw new IllegalArgumentException();
        this.bind = bindAddress;
        return this;
    }

    public RestAPIMainBuilder withPort(final int port) {
        if (port < 0) throw new IllegalArgumentException();
        this.port = port;
        return this;
    }

    public RestAPIMainBuilder withStage(final Stage stage) {
        if (stage == null) throw new IllegalArgumentException();
        this.stage = stage;
        return this;
    }

    public RestAPIMainBuilder withApiContext(final String apiContext) {
        if (apiContext == null) throw new IllegalArgumentException();
        this.apiContext = apiContext;
        return this;
    }

    public RestAPIMainBuilder withAdditionalModules(final Iterable<Module> additionalModules) {
        this.additionalModules.clear();
        addModules(additionalModules);
        return this;
    }

    public RestAPIMainBuilder addModule(final Module module) {
        additionalModules.add(module);
        return this;
    }

    public RestAPIMainBuilder addModules(final Iterable<Module> additionalModules) {
        for (final Module module : additionalModules) addModule(module);
        return this;
    }

    public RestAPIMain build() {
        return new RestAPIMain(port, bind, apiContext, stage, additionalModules);
    }

}
