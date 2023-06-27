package dev.getelements.elements.rt.jersey;

import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Collection;
import java.util.Map;

import static dev.getelements.elements.rt.jersey.GenericMultipartFeature.*;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;

@Singleton
@Produces("multipart/*")
public class GenericMultipartWriter implements MessageBodyWriter<Collection<?>> {

    private final Providers providers;

    public GenericMultipartWriter(@Context final Providers providers) {
        this.providers = providers;
    }

    @Override
    public boolean isWriteable(final Class<?> type,
                               final Type genericType,
                               final Annotation[] annotations,
                               final MediaType mediaType) {
        return Collection.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(final Collection<?> collection,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {

        final var delegate = providers.getMessageBodyWriter(
                MultiPart.class,
                MultiPart.class,
                annotations,
                mediaType
        );

        final var multipart = new MultiPart(mediaType);

        collection.stream()
                .map(o -> toBodyPart(o, mediaType))
                .forEach(multipart::bodyPart);

        delegate.writeTo(
                multipart,
                MultiPart.class,
                null,
                new Annotation[]{},
                mediaType,
                httpHeaders,
                entityStream
        );

    }

    private BodyPart toBodyPart(final Object o, final MediaType requestMediaType) {

        if (!(o instanceof Map)) {
            throw new IllegalArgumentException("Must specify a collection of Map instances.");
        }

        final var map = (Map<?,?>) o;

        final var rawType = map.get(TYPE);
        final var rawDisposition = map.get(DISPOSITION);
        final var rawEntity = map.get(ENTITY);

        final BodyPart bodyPart;

        if (rawType == null) {
            bodyPart = new BodyPart(rawEntity, MediaType.TEXT_PLAIN_TYPE);
        } else {
            final var mediaType = MediaType.valueOf(rawType.toString());
            bodyPart = new BodyPart(rawEntity, mediaType);
        }

        if (rawDisposition != null) {
            try {

                final var contentDisposition = MULTIPART_FORM_DATA_TYPE.isCompatible(requestMediaType)
                    ? new FormDataContentDisposition(rawDisposition.toString())
                    : new ContentDisposition(rawDisposition.toString());

                bodyPart.setContentDisposition(contentDisposition);

            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }

        return bodyPart;

    }

}
