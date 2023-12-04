package dev.getelements.elements.service;

import dev.getelements.elements.model.schema.EditorSchema;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.json.JsonSchema;
import dev.getelements.elements.service.schema.*;
import dev.getelements.elements.util.MetadataSpecBuilder;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.dozer.loader.api.BeanMappingBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static dev.getelements.elements.Constants.API_OUTSIDE_URL;
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.*;
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.STRING;
import static org.dozer.loader.api.FieldsMappingOptions.*;

public class ServicesDozerMapperProvider implements Provider<Mapper> {

    private Provider<String> apiOutsideUrlProvider;

    @Override
    public Mapper get() {

        final var apiOutsideUrl = getApiOutsideUrlProvider().get();

        final BeanMappingBuilder beanMappingBuilder = new BeanMappingBuilder() {
            @Override
            protected void configure() {

                mapping(MetadataSpec.class, JsonSchema.class)
                        .fields("name", "$id",
                                customConverter(JsonSchemaIdConverter.class, apiOutsideUrl),
                                oneWay()
                        )
                        .fields("type", "type",
                                customConverter(JsonSchemaTypeConverter.class)
                        )
                        .fields("name", "title")
                        .fields("name", "description")
                        .fields("properties", "properties",
                                customConverter(JsonSchemaPropertiesConverter.class),
                                oneWay()
                        )
                        .fields("properties","required",
                                customConverter(JsonSchemaRequiredPropertiesConverter.class),
                                oneWay()
                        );

                mapping(MetadataSpec.class, EditorSchema.class)
                        .fields(this_(), "schema")
                        .fields(this_(), "layout",
                                customConverter(EditorSchemaLayoutConverter.class),
                                oneWay()
                        )
                        .fields(this_(),"data",
                                customConverter(EditorSchemaDataConverter.class),
                                oneWay()
                        );

            }
        };

        final DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
        dozerBeanMapper.addMapping(beanMappingBuilder);
        return dozerBeanMapper;

    }

    public Provider<String> getApiOutsideUrlProvider() {
        return apiOutsideUrlProvider;
    }

    @Inject
    public void setApiOutsideUrlProvider(@Named(API_OUTSIDE_URL) final Provider<String> apiOutsideUrlProvider) {
        this.apiOutsideUrlProvider = apiOutsideUrlProvider;
    }

}
