package com.namazustudios.socialengine.dao.mongo.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.namazustudios.socialengine.model.blockchain.bsc.Web3jWallet;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

import java.io.IOException;

public class MongoBscWalletConverter implements CustomConverter {

    @Override
    public Object convert(final Object existingDestinationFieldValue, final Object sourceFieldValue,
                          final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == byte[].class && destinationClass == byte[].class) {
            return sourceFieldValue;
        } else if (sourceClass == byte[].class && destinationClass == Web3jWallet.class) {
            try {
                return sourceFieldValue == null ? null : Web3jWallet.OBJECT_MAPPER.readValue((byte[]) sourceFieldValue, Web3jWallet.class);
            } catch (IOException e) {
                return null;
            }
        } else if (sourceClass == Web3jWallet.class && destinationClass == byte[].class) {
            try {
                return sourceFieldValue == null ? null : Web3jWallet.OBJECT_MAPPER.writeValueAsBytes(sourceFieldValue);
            } catch (JsonProcessingException e) {
                return null;
            }
        } else {
            throw new MappingException("No conversion exists betweeen " + sourceClass + " and " + destinationClass);
        }
    }

}