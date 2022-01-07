package com.namazustudios.socialengine.dao.mongo.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.namazustudios.socialengine.model.blockchain.neo.Nep6Wallet;
import io.neow3j.wallet.Wallet;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

import java.io.IOException;

public class MongoNeoWalletConverter implements CustomConverter {

    @Override
    public Object convert(final Object existingDestinationFieldValue, final Object sourceFieldValue,
                          final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == byte[].class && destinationClass == byte[].class) {
            return sourceFieldValue;
        } else if (sourceClass == byte[].class && destinationClass == Nep6Wallet.class) {
            try {
                return sourceFieldValue == null ? null : Wallet.OBJECT_MAPPER.readValue((byte[]) sourceFieldValue, Nep6Wallet.class);
            } catch (IOException e) {
                return null;
            }
        } else if (sourceClass == Nep6Wallet.class && destinationClass == byte[].class) {
            try {
                return sourceFieldValue == null ? null : Wallet.OBJECT_MAPPER.writeValueAsBytes(sourceFieldValue);
            } catch (JsonProcessingException e) {
                return null;
            }
        } else {
            throw new MappingException("No conversion exists betweeen " + sourceClass + " and " + destinationClass);
        }
    }

}