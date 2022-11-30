package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.FormidiumInvestorDao;
import com.namazustudios.socialengine.dao.mongo.model.formidium.MongoFormidiumInvestor;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.util.PaginationWalker;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoFormidiumInvestorDaoTest {

    private UserTestFactory userTestFactory;

    private FormidiumInvestorDao formidiumInvestorDao;

    private List<User> users;

    private Map<String, FormidiumInvestor> intermediates = new ConcurrentHashMap<>();

    @BeforeClass
    public void generateTestUsers() {
        users = IntStream
            .range(0, 100)
            .mapToObj(i -> getUserTestFactory().createTestUser())
            .collect(toUnmodifiableList());
    }

    @DataProvider
    public Object[][] allTestUsers() {
        return users
                .stream()
                .map(u -> new Object[]{u})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] allFormidiumInvestors() {
        return intermediates.values()
                .stream()
                .map(i -> new Object[]{i})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "allTestUsers")
    public void createInvestor(final User user) {
        final var mockFormidiumId = UUID.randomUUID().toString();
        final var investor = getFormidiumInvestorDao().createInvestor(mockFormidiumId, user.getId());
        assertNotNull(investor.getId());
        assertEquals(investor.getUser(), user);
        assertEquals(investor.getFormidiumInvestorId(), mockFormidiumId);
        intermediates.put(investor.getFormidiumInvestorId(), investor);
    }

    @Test(dataProvider = "allTestUsers", dependsOnMethods = "createInvestor", expectedExceptions = DuplicateException.class)
    public void checkCreateDuplicateFails(final User user) {
        final var mockFormidiumId = UUID.randomUUID().toString();
        getFormidiumInvestorDao().createInvestor(mockFormidiumId, user.getId());
    }

    @Test(dataProvider = "allFormidiumInvestors", dependsOnMethods = "checkCreateDuplicateFails")
    public void findFormidiumInvestorById(final FormidiumInvestor formidiumInvestor) {
        final var result = getFormidiumInvestorDao().findFormidiumInvestor(formidiumInvestor.getId());
        assertTrue(result.isPresent());
        assertEquals(result.get(), formidiumInvestor);
    }

    @Test(dataProvider = "allFormidiumInvestors", dependsOnMethods = "checkCreateDuplicateFails")
    public void findFormidiumInvestorByIdAndUser(final FormidiumInvestor formidiumInvestor) {
        final var result = getFormidiumInvestorDao().findFormidiumInvestor(formidiumInvestor.getId(), formidiumInvestor.getUser().getId());
        assertTrue(result.isPresent());
        assertEquals(result.get(), formidiumInvestor);
    }

    @Test(dataProvider = "allFormidiumInvestors", dependsOnMethods = "checkCreateDuplicateFails", expectedExceptions = NotFoundException.class)
    public void findFormidiumInvestorByIdAndUserMismatch(final FormidiumInvestor formidiumInvestor) {
        getFormidiumInvestorDao().findFormidiumInvestor(formidiumInvestor.getId(), new ObjectId().toString());
    }


    @Test(dependsOnMethods = "checkCreateDuplicateFails")
    public void testGetAllInvestors() {
        final var result = new PaginationWalker().toList((offset, count) -> getFormidiumInvestorDao().getFormidiumInvestors(null, offset, count));
        assertEquals(result.size(), intermediates.size());
        assertTrue(result.containsAll(intermediates.values()));
    }

    @Test(dataProvider = "allFormidiumInvestors", dependsOnMethods = "checkCreateDuplicateFails")
    public void testGetAllInvestorsPerUser(final FormidiumInvestor formidiumInvestor) {

        final var pagination = getFormidiumInvestorDao().getFormidiumInvestors(formidiumInvestor.getUser().getId(), 0, 100);
        assertEquals(pagination.getTotal(), 1);

        final var result = pagination.getObjects().get(0);
        assertEquals(result, formidiumInvestor);

    }

    @Test(dataProvider = "allFormidiumInvestors", dependsOnMethods = {
            "findFormidiumInvestorById",
            "findFormidiumInvestorByIdAndUser",
            "findFormidiumInvestorByIdAndUserMismatch",
            "testGetAllInvestors",
            "testGetAllInvestorsPerUser"
    })
    public void testDeleteInvestor(final FormidiumInvestor formidiumInvestor) {
        getFormidiumInvestorDao().deleteFormidiumInvestor(formidiumInvestor.getId());
    }

    @Test(dataProvider = "allFormidiumInvestors", dependsOnMethods = "testDeleteInvestor", expectedExceptions = NotFoundException.class)
    public void testGetDeletedInvestorFails(final FormidiumInvestor formidiumInvestor) {
        getFormidiumInvestorDao().getFormidiumInvestor(formidiumInvestor.getId());
    }

    @Test(dataProvider = "allFormidiumInvestors", dependsOnMethods = "testDeleteInvestor", expectedExceptions = NotFoundException.class)
    public void testGetDeletedByUserIdInvestorFails(final FormidiumInvestor formidiumInvestor) {
        getFormidiumInvestorDao().getFormidiumInvestor(formidiumInvestor.getId(), formidiumInvestor.getUser().getId());
    }

    @Test(dataProvider = "allFormidiumInvestors", dependsOnMethods = "testDeleteInvestor", expectedExceptions = NotFoundException.class)
    public void testDeleteInvestorTwiceFails(final FormidiumInvestor formidiumInvestor) {
        getFormidiumInvestorDao().deleteFormidiumInvestor(formidiumInvestor.getId());
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public FormidiumInvestorDao getFormidiumInvestorDao() {
        return formidiumInvestorDao;
    }

    @Inject
    public void setFormidiumInvestorDao(FormidiumInvestorDao formidiumInvestorDao) {
        this.formidiumInvestorDao = formidiumInvestorDao;
    }

}
