package dev.getelements.elements.rt.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class OctetStreamJsonMessageBodyReader<T> implements MessageBodyReader<T> {

    @Context
    private Providers providers;

    @Override
    public boolean isReadable(final Class<?> type,
                              final Type genericType,
                              final Annotation[] annotations,
                              final MediaType mediaType) {
        return MediaType.APPLICATION_OCTET_STREAM_TYPE.isCompatible(mediaType);
    }

    @Override
    public T readFrom(final Class<T> type,
                      final Type genericType,
                      final Annotation[] annotations,
                      final MediaType mediaType,
                      final MultivaluedMap<String, String> httpHeaders,
                      final InputStream entityStream) throws IOException, WebApplicationException {
        final ContextResolver<ObjectMapper> resolver = providers.getContextResolver(ObjectMapper.class, APPLICATION_JSON_TYPE);
        final ObjectMapper objectMapper = resolver.getContext(type);
        return objectMapper.readValue(entityStream, type);
    }

}
