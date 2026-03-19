package dev.getelements.elements.sdk.test.element.rs;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ElementServiceImplementation
@ElementServiceExport(value = SimpleService.class)
public class SimpleServiceImplementation implements SimpleService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleServiceImplementation.class);

    @Inject
    public void setDatabaseName(@Named("dev.getelements.elements.mongo.database.name") final String databaseName) {
        logger.info("Using database: {}", databaseName);
    }

}
