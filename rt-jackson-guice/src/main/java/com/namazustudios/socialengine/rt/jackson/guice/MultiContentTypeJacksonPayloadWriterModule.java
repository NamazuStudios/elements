package com.namazustudios.socialengine.rt.jackson.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.jackson.ObjectMapperPayloadWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;

import static com.google.inject.multibindings.MapBinder.newMapBinder;

public class MultiContentTypeJacksonPayloadWriterModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(MultiContentTypeJacksonPayloadWriterModule.class);

    @Override
    protected void configure() {

        final MapBinder<String, PayloadWriter> writers = newMapBinder(binder(), String.class, PayloadWriter.class);

        writers.addBinding("application/json")
               .toProvider(writerProvider(getProvider(ObjectMapper.class)));

        try {
            Class.forName("com.fasterxml.jackson.dataformat.yaml.YAMLMapper");
            final Provider<YAMLMapper> yamlMapperProvider = getProvider(YAMLMapper.class);
            writers.addBinding("application/yaml").toProvider(writerProvider(yamlMapperProvider));
            logger.info("YAML Support Enabled.");
        } catch (ClassNotFoundException ex) {
            logger.info("YAML Support Disabled.");
        }

        try {
            Class.forName("com.fasterxml.jackson.dataformat.xml.XmlMapper");
            final Provider<XmlMapper> xmlMapperProvider = getProvider(XmlMapper.class);
            writers.addBinding("application/xml").toProvider(writerProvider(xmlMapperProvider));
            logger.info("XML Support Enabled.");
        } catch (ClassNotFoundException ex) {
            logger.info("XML Support Disabled.");
        }

    }

    private Provider<ObjectMapperPayloadWriter> writerProvider(final Provider<? extends ObjectMapper> objectMapperProvider) {
        return () -> {
            final ObjectMapperPayloadWriter objectMapperPayloadWriter = new ObjectMapperPayloadWriter();
            objectMapperPayloadWriter.setObjectMapper(objectMapperProvider.get());
            return objectMapperPayloadWriter;
        };
    }

}
