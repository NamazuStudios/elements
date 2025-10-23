package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static dev.getelements.elements.service.largeobject.LargeObjectServiceTestFactory.TEST_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Guice(modules = LargeObjectServiceTestModule.class)
public class AnonymousLargeObjectServiceTest extends LargeObjectServiceTestBase{

    @Inject
    private AnonLargeObjectService anonLargeObjectService;

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToWrite() throws IOException {
        anonLargeObjectService.writeLargeObjectContent(TEST_ID, "shouldNotAllowToWrite");
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToUpdate() {
        anonLargeObjectService.updateLargeObject(TEST_ID, factory.defaultUpdateRequestWithWildcardAccess(false, false, false));
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToCreate() {
        anonLargeObjectService.createLargeObject(factory.defaultCreateRequestWithWildcardAccess(false, false, false));
    }

    @Test
    public void shouldGetLargeObject() {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.wildcardLargeObject()));

        LargeObject result = anonLargeObjectService.getLargeObject(TEST_ID);

        assertNotNull(result);
        assertTrue(result.getAccessPermissions().getRead().isWildcard());
    }

    @Test
    public void shouldReadLargeObject() throws IOException {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.wildcardLargeObject()));
        when(largeObjectBucket.readObject(TEST_ID)).then(a -> mock(FileInputStream.class));

        InputStream result = anonLargeObjectService.readLargeObjectContent(TEST_ID);

        assertNotNull(result);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowReadLargeObject() throws IOException {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.defaultLargeObjectWithAccess(false, true, true)));
        anonLargeObjectService.readLargeObjectContent(TEST_ID);
    }

}
