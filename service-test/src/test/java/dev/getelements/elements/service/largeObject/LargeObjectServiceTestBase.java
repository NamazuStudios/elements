package dev.getelements.elements.service.largeObject;

import dev.getelements.elements.dao.LargeObjectBucket;
import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.service.largeobject.LargeObjectAccessUtils;
import dev.getelements.elements.util.ValidationHelper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import javax.inject.Inject;

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
    protected LargeObjectAccessUtils largeObjectAccessUtils;

    protected LargeObjectServiceTestFactory factory = new LargeObjectServiceTestFactory();

    @BeforeClass
    public void setup() {
        final var injector = createInjector(new LargeObjectServiceTestModule());
        injector.injectMembers(this);
    }

    @BeforeMethod
    public void resetMocks() {
        reset(largeObjectDao, largeObjectBucket);
    }
}
