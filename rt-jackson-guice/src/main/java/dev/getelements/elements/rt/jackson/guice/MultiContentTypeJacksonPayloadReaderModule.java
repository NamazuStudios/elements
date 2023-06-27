package dev.getelements.elements.rt.jackson.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.jackson.ObjectMapperPayloadReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;

import static com.google.inject.multibindings.MapBinder.newMapBinder;

public class MultiContentTypeJacksonPayloadReaderModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(MultiContentTypeJacksonPayloadReaderModule.class);

    @Override
    public void configure() {

    final MapBinder<String, PayloadReader> writers = newMapBinder(binder(), String.class, PayloadReader.class);

        writers.addBinding("application/json")
                .toProvider(readerProvider(getProvider(ObjectMapper.class)));

        try {
            Class.forName("com.fasterxml.jackson.dataformat.yaml.YAMLMapper");
            final Provider<YAMLMapper> yamlMapperProvider = getProvider(YAMLMapper.class);
            writers.addBinding("application/yaml").toProvider(readerProvider(yamlMapperProvider));
            logger.info("YAML Support Enabled.");
        } catch (ClassNotFoundException ex) {
            logger.info("YAML Support Disabled.");
        }

        try {
            Class.forName("com.fasterxml.jackson.dataformat.xml.XmlMapper");
            final Provider<XmlMapper> xmlMapperProvider = getProvider(XmlMapper.class);
            writers.addBinding("application/xml").toProvider(readerProvider(xmlMapperProvider));
            logger.info("XML Support Enabled.");
        } catch (ClassNotFoundException ex) {
            logger.info("XML Support Disabled.");
        }

    }

    private Provider<ObjectMapperPayloadReader> readerProvider(final Provider<? extends ObjectMapper> objectMapperProvider) {
        return () -> {
            final ObjectMapperPayloadReader objectMapperPayloadReader = new ObjectMapperPayloadReader();
            objectMapperPayloadReader.setObjectMapper(objectMapperProvider.get());
            return objectMapperPayloadReader;
        };
    }

}
