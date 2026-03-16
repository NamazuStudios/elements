package dev.getelements.elements.sdk.mongo;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.sql.Timestamp;

/**
 * Used by the default {@link com.mongodb.client.MongoClient} to handle serialization of {@link Timestamp} instances
 * storing each value as a 64-bit long. More specifically, this uses the value of {@link Timestamp#getTime()} to store
 * the value, and constructs a new instance using {@link Timestamp#Timestamp(long)}.
 *
 * This enforces an opinion of how to properly store {@link Timestamp} instances in the mongo database. It should be
 * known that the default {@link com.mongodb.client.MongoClient} uses it which may or may not be desired for a client
 * application.
 */
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
