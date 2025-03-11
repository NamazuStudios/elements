package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import org.testng.annotations.BeforeMethod;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;

import static com.google.inject.Guice.createInjector;
import static org.mockito.Mockito.reset;

public class LargeObjectServiceTestBase {

    @Inject
    protected ValidationHelper validationHelper;

    @Inject
    protected LargeObjectDao largeObjectDao;

    @Inject
    protected LargeObjectBucket largeObjectBucket;

    @Inject
    protected AccessRequestUtils accessRequestUtils;

    @Inject
    protected Client client;

    protected LargeObjectServiceTestFactory factory = new LargeObjectServiceTestFactory();

    @BeforeMethod
    public void resetMocks() {
        reset(largeObjectDao, largeObjectBucket);
    }

}
