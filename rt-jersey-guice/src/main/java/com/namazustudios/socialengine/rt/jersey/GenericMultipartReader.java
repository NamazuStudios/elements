package com.namazustudios.socialengine.rt.jersey;

import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.internal.MultiPartReaderClientSide;
import org.glassfish.jersey.media.multipart.internal.MultiPartReaderServerSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Singleton
@Consumes("multipart/*")
public class GenericMultipartReader implements MessageBodyReader<Collection<?>> {

    private static final Logger logger = LoggerFactory.getLogger(GenericMultipartReader.class);

    private final Providers providers;

    public GenericMultipartReader(final @Context Providers providers) {
        this.providers = providers;
    }

    @Override
    public boolean isReadable(final Class<?> type,
                              final Type genericType,
                              final Annotation[] annotations,
                              final MediaType mediaType) {
        return Collection.class.isAssignableFrom(type);
    }

    @Override
    public Collection<?> readFrom(
            final Class<Collection<?>> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, String> httpHeaders,
            final InputStream entityStream)
            throws IOException, WebApplicationException {

        final var delegate = providers.getMessageBodyReader(
            MultiPart.class,
            MultiPart.class,
            annotations,
            mediaType);

        final var multipart = delegate.readFrom(
                MultiPart.class,
                null,
                new Annotation[]{},
                mediaType,
                httpHeaders,
                entityStream
        );

        return multipart.getBodyParts()
            .stream()
            .map(this::toMap)
            .collect(toList());

    }

    private Map<String, Object> toMap(final BodyPart bodyPart) {

        final var map = new LinkedHashMap<String, Object>();
        final var type = bodyPart.getMediaType();
        final var disposition = bodyPart.getContentDisposition();
        final var entity = bodyPart.getEntity();

        map.put(GenericMultipartFeature.TYPE, type.getType());

        if (disposition != null) {
            map.put(GenericMultipartFeature.DISPOSITION, disposition.toString());
        }

        if (entity != null) {
            map.put(GenericMultipartFeature.ENTITY, entity);
        }

        return map;

    }

}
