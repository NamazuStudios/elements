package dev.getelements.elements.service.util;

import dev.getelements.elements.common.util.mapstruct.MapstructMapperRegistryBuilder;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;

public class ServicesMapperRegistryProvider implements Provider<MapperRegistry> {

    private Provider<String> apiOutsideUrlProvider;

    @Override
    public MapperRegistry get() {

        final var apiOutsideUrl = getApiOutsideUrlProvider().get();

        return new MapstructMapperRegistryBuilder()
                .withPackages("dev.getelements.elements.service.util")
                .withCreationListener(
                        JsonSchemaMapper.class,
                        jsonSchemaMapper -> jsonSchemaMapper.setApiOutsideUrl(apiOutsideUrl)
                )
                .withCreationListener(
                        EditorSchemaMapper.class,
                        editorSchemaMapper -> editorSchemaMapper.setApiOutsideUrl(apiOutsideUrl)
                )
                .build();

    }

    public Provider<String> getApiOutsideUrlProvider() {
        return apiOutsideUrlProvider;
    }

    @Inject
    public void setApiOutsideUrlProvider(@Named(API_OUTSIDE_URL) final Provider<String> apiOutsideUrlProvider) {
        this.apiOutsideUrlProvider = apiOutsideUrlProvider;
    }

}
