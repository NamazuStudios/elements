package com.namazustudios.socialengine.dao.mongo.converter;

import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoSmartContractAddress;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContractAddress;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class MongoSmartContractAddressesConverter implements CustomConverter {

    @Override
    public Object convert(
            final Object existingDestinationFieldValue,
            final Object sourceFieldValue,
            final Class<?> destinationClass,
            final Class<?> sourceClass) {
        if (Map.class.isAssignableFrom(sourceClass) && List.class.isAssignableFrom(destinationClass)) {
            return sourceFieldValue == null
                    ? null
                    : ((Map<?,?>)sourceFieldValue)
                        .entrySet()
                        .stream()
                        .map(this::toMongoAddress)
                        .collect(toList());
        } else if (List.class.isAssignableFrom(sourceClass) && Map.class.isAssignableFrom(destinationClass)) {
            return sourceFieldValue == null
                    ? null
                    : ((List<?>)sourceFieldValue)
                        .stream()
                        .collect(toMap(this::toNetwork, this::toAddress));
        } else {
            throw new MappingException("No conversion exists between " + sourceClass + " and " + destinationClass);
        }
    }

    private BlockchainNetwork toNetwork(final Object object) {
        try {
            final var mongoSmartContractAddress = (MongoSmartContractAddress) object;
            return mongoSmartContractAddress.getNetwork();
        } catch (ClassCastException ex) {
            throw new MappingException(ex);
        }
    }

    private SmartContractAddress toAddress(final Object object) {
        try {
            final var mongoSmartContractAddress = (MongoSmartContractAddress) object;
            final var address = mongoSmartContractAddress.getAddress();
            final var smartContractAddress = new SmartContractAddress();
            smartContractAddress.setAddress(address);
            return smartContractAddress;
        } catch (ClassCastException ex) {
            throw new MappingException(ex);
        }
    }

    private MongoSmartContractAddress toMongoAddress(final Map.Entry<?,?> entry) {
        try {
            final var network = (BlockchainNetwork) entry.getKey();
            final var smartContractAddress = (SmartContractAddress) entry.getValue();
            return MongoSmartContractAddress.fromNetworkAndAddress(network, smartContractAddress);
        } catch (ClassCastException ex) {
            throw new MappingException(ex);
        }
    }

}
