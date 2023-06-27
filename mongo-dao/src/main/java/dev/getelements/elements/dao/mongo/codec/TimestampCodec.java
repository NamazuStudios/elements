package dev.getelements.elements.dao.mongo.codec;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.sql.Timestamp;

public class TimestampCodec implements Codec<Timestamp> {

    @Override
    public Timestamp decode(final BsonReader reader, final DecoderContext decoderContext) {
        final var timestamp = reader.readDateTime();
        return new Timestamp(timestamp);
    }

    @Override
    public void encode(final BsonWriter writer, final Timestamp value, final EncoderContext encoderContext) {
        writer.writeDateTime(value.getTime());
    }

    @Override
    public Class<Timestamp> getEncoderClass() {
        return Timestamp.class;
    }

}
