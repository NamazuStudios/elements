package dev.getelements.elements.dao.mongo.ucode;

import dev.getelements.elements.sdk.dao.UniqueCodeDao;
import dev.getelements.elements.sdk.model.ucode.UniqueCode;

import java.util.Optional;

public class MongoUniqueCodeDao implements UniqueCodeDao {

    @Override
    public UniqueCode generateCode(GenerationParameters parameters) {
        return null;
    }

    @Override
    public Optional<UniqueCode> findCode(String code) {
        return Optional.empty();
    }

    @Override
    public void resetTimeout(String code, long timeout) {

    }

    @Override
    public void releaseCode(String code) {

    }

    @Override
    public boolean tryReleaseCode(String code) {
        return false;
    }

}
