package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.mongo.model.goods.MongoInventoryItemId;
import org.bson.types.ObjectId;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

import static org.testng.AssertJUnit.assertEquals;

public class TestMongoInventoryItemMongoIndexPlannerId {


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

        AssertJUnit.assertEquals(user, first.getUserObjectId());
        AssertJUnit.assertEquals(item, first.getItemObjectId());
        AssertJUnit.assertEquals(priority, first.getPriority());

        final MongoInventoryItemId second = new MongoInventoryItemId(first.toHexString());

        AssertJUnit.assertEquals(user, second.getUserObjectId());
        AssertJUnit.assertEquals(item, second.getItemObjectId());
        AssertJUnit.assertEquals(priority, second.getPriority());

        AssertJUnit.assertEquals(first, second);

    }

}
