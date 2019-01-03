package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import org.bson.types.ObjectId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

import static org.testng.AssertJUnit.assertEquals;

public class TestMongoInventoryItemId {


    @DataProvider
    public Object[][] getPriorities() {
        return IntStream
            .range(0, 10)
            .mapToObj(i -> new Object[]{i})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getPriorities")
    public void testToHexStringAndBack(final int priority) {

        final ObjectId user = new ObjectId();
        final ObjectId item = new ObjectId();

        final MongoInventoryItemId first = new MongoInventoryItemId(user, item, priority);

        assertEquals(user, first.getUserObjectId());
        assertEquals(item, first.getItemObjectId());
        assertEquals(priority, first.getPriority());

        final MongoInventoryItemId second = new MongoInventoryItemId(first.toHexString());

        assertEquals(user, second.getUserObjectId());
        assertEquals(item, second.getItemObjectId());
        assertEquals(priority, second.getPriority());

        assertEquals(first, second);

    }

}
