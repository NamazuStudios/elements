package dev.getelements.elements.rt.jersey;

import com.opencsv.CSVWriter;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CsvMessageBodyWriter implements MessageBodyWriter<Object> {

    private static final MediaType TEXT_CSV = MediaType.valueOf("text/csv");

    private Function<Writer, CSVWriter> csvWriterConstructor;

    private Provider<MappingStrategy<Object>> mappingStrategyProvider;

    @Override
    public boolean isWriteable(
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return type.isAssignableFrom(Iterable.class)  && TEXT_CSV.isCompatible(mediaType);
    }

    @Override
    public void writeTo(
            final Object o,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream) throws IOException, WebApplicationException {

        if (o == null) {
            return;
        }

        final var charsetName = mediaType
                .getParameters()
                .getOrDefault("charset", UTF_8.name());

        final var mappingStrategy = getMappingStrategyProvider().get();

        try (var ioWriter = new OutputStreamWriter(entityStream, charsetName);
             var csvWriter = getCsvWriterConstructor().apply(ioWriter)) {

            final var header = mappingStrategy.generateHeader(o);
            csvWriter.writeNext(header);

            final var iterable = (Iterable<?>) o;
            final var iterator = iterable.iterator();

        } catch (CsvRequiredFieldEmptyException e) {
            throw new WebApplicationException(e);
        }

    }

    public Function<Writer, CSVWriter> getCsvWriterConstructor() {
        return csvWriterConstructor;
    }

    @Inject
    public void setCsvWriterConstructor(Function<Writer, CSVWriter> csvWriterConstructor) {
        this.csvWriterConstructor = csvWriterConstructor;
    }

    public Provider<MappingStrategy<Object>> getMappingStrategyProvider() {
        return mappingStrategyProvider;
    }

    @Inject
    public void setMappingStrategyProvider(Provider<MappingStrategy<Object>> mappingStrategyProvider) {
        this.mappingStrategyProvider = mappingStrategyProvider;
    }

}
