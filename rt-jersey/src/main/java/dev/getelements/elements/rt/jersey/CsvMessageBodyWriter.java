package dev.getelements.elements.rt.jersey;

import com.opencsv.CSVWriter;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.exceptions.CsvChainedException;
import com.opencsv.exceptions.CsvFieldAssignmentException;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
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
        return TEXT_CSV.isCompatible(mediaType);
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

        if (o instanceof Iterable) {
            writeIterable(o, mediaType, entityStream);
        } else if (o != null) {
            writeSingleObject(o, mediaType, entityStream);
        }

    }

    private void writeIterable(final Object o,
                               final MediaType mediaType,
                               final OutputStream entityStream) throws IOException {

        final var charsetName = mediaType
                .getParameters()
                .getOrDefault("charset", UTF_8.name());

        final var mappingStrategy = getMappingStrategyProvider().get();

        try (var ioWriter = new OutputStreamWriter(entityStream, charsetName);
             var csvWriter = getCsvWriterConstructor().apply(ioWriter)) {

            final var iterator = ((Iterable<?>) o).iterator();

            Object value = null;

            while (value == null && iterator.hasNext()) {
                value = iterator.next();
            }

            if (value == null) {
                return;
            }

            mappingStrategy.setType(value.getClass());

            final var header = mappingStrategy.generateHeader(value);
            csvWriter.writeNext(header);

            var line = mappingStrategy.transmuteBean(value);
            csvWriter.writeNext(line);

            while (iterator.hasNext()) {

                value = iterator.next();

                if (value != null) {
                    line = mappingStrategy.transmuteBean(value);
                    csvWriter.writeNext(line);
                }

            }

        } catch (CsvChainedException | CsvFieldAssignmentException e) {
            throw new WebApplicationException(e);
        }

    }

    private void writeSingleObject(final Object o,
                                   final MediaType mediaType,
                                   final OutputStream entityStream) throws IOException {

        final var charsetName = mediaType
                .getParameters()
                .getOrDefault("charset", UTF_8.name());

        final var mappingStrategy = getMappingStrategyProvider().get();
        mappingStrategy.setType(o.getClass());

        try (var ioWriter = new OutputStreamWriter(entityStream, charsetName);
             var csvWriter = getCsvWriterConstructor().apply(ioWriter)) {

            final var header = mappingStrategy.generateHeader(o);
            csvWriter.writeNext(header);

            final var line = mappingStrategy.transmuteBean(o);
            csvWriter.writeNext(line);

        } catch (CsvChainedException | CsvFieldAssignmentException e) {
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
