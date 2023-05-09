package dev.getelements.elements.dao.mongo.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.getelements.elements.model.blockchain.bsc.Web3jWallet;
import dev.getelements.elements.rt.exception.InternalException;
import org.dozer.CustomConverter;
import org.dozer.MappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class MongoBscWalletConverter implements CustomConverter {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static byte[] asBytes(final Web3jWallet wallet) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(wallet);
        } catch (JsonProcessingException ex) {
            throw new InternalException(ex);
        }
    }

    @Override
    public Object convert(final Object existingDestinationFieldValue, final Object sourceFieldValue,
                          final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == byte[].class && destinationClass == byte[].class) {
            return sourceFieldValue;
        } else if (sourceClass == byte[].class && destinationClass == Web3jWallet.class) {
            try {
                return sourceFieldValue == null ? null : MongoBscWalletConverter.OBJECT_MAPPER.readValue((byte[]) sourceFieldValue, Web3jWallet.class);
            } catch (IOException e) {
                return null;
            }
        } else if (sourceClass == Web3jWallet.class && destinationClass == byte[].class) {
            try {
                return sourceFieldValue == null ? null : MongoBscWalletConverter.OBJECT_MAPPER.writeValueAsBytes(sourceFieldValue);
            } catch (JsonProcessingException e) {
                return null;
            }
        } else {
            throw new MappingException("No conversion exists between " + sourceClass + " and " + destinationClass);
        }
    }

}
