package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.BsonParser;

import javax.inject.Provider;

/**
 * Created by patricktwohig on 9/29/15.
 */
public class BSONObjectMapperProvider implements Provider<ObjectMapper> {

    private BSONObjectMapperProvider() {}

    private static final BSONObjectMapperProvider INSTANCE = new BSONObjectMapperProvider();

    public static BSONObjectMapperProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public ObjectMapper get() {
        final BsonFactory bsonFactory = new BsonFactory();
        bsonFactory.disable(BsonGenerator.Feature.ENABLE_STREAMING);
        bsonFactory.disable(BsonParser.Feature.HONOR_DOCUMENT_LENGTH);
        return new ObjectMapper(bsonFactory);
    }

}
