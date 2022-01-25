package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.DistinctInventoryItemDao;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.goods.ItemCategory;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.namazustudios.socialengine.model.goods.ItemCategory.DISTINCT;

@Guice(modules = IntegrationTestModule.class)
public class MongoDistinctInventorItemDaoTest {

    private static final int ITEM_COUNT = 2;

    private static final int USER_COUNT = 5;

    @Inject
    private DistinctInventoryItemDao underTest;

    @Inject
    private ItemTestFactory itemTestFactory;

    @Inject
    private UserTestFactory userTestFactory;

    @Inject
    private ProfileTestFactory profileTestFactory;

    @Inject
    private ApplicationTestFactory applicationTestFactory;

    private List<Item> itemList;

    private List<User> userList;

    private List<Profile> profileList;

    private Application application;

    @Test
    public void testCreateDistinctInventoryItem() {

        itemList = new ArrayList<>();
        userList = new ArrayList<>();
        profileList = new ArrayList<>();

        application = applicationTestFactory.createMockApplication("Distinct Items");

        for (int i = 0; i < ITEM_COUNT; ++i) {
            final var item = itemTestFactory.createTestItem(DISTINCT);
            itemList.add(item);
        }

        for (int i = 0; i < USER_COUNT; ++i) {

            final var user = userTestFactory.createTestUser();
            userList.add(user);

            final var profile = profileTestFactory.makeMockProfile(user, application);
            profileList.add(profile);

        }


    }

}
