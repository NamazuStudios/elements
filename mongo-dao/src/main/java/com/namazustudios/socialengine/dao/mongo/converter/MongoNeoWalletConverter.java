package com.namazustudios.socialengine.dao.mongo.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoWallet;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoProgressId;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.nep6.NEP6Wallet;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

import java.io.IOException;
import java.util.Base64;

public class MongoNeoWalletConverter implements CustomConverter {

    @Override
    public Object convert(final Object existingDestinationFieldValue, final Object sourceFieldValue,
                          final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == String.class && destinationClass == String.class) {
            return sourceFieldValue;
        } else if (sourceClass == String.class && destinationClass == NEP6Wallet.class) {
            try {
                return sourceFieldValue == null ? null : Wallet.OBJECT_MAPPER.readValue(Base64.getDecoder().decode(sourceFieldValue.toString()), NEP6Wallet.class);
            } catch (IOException e) {
                return null;
            }
        } else if (sourceClass == NEP6Wallet.class && destinationClass == String.class) {
            try {
                return sourceFieldValue == null ? null : Base64.getEncoder().encodeToString(Wallet.OBJECT_MAPPER.writeValueAsBytes(sourceFieldValue));
            } catch (JsonProcessingException e) {
                return null;
            }
        } else {
            throw new MappingException("No conversion exists betweeen " + sourceClass + " and " + destinationClass);
        }
    }

}