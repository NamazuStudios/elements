package dev.getelements.elements.dao.mongo.mapper;

import org.bson.types.ObjectId;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class PropertyConvertersTest {

    private final PropertyConverters converters = new PropertyConverters();

    @Test
    public void toObjectId_null_returnsNull() {
        assertNull(converters.toObjectId(null));
    }

    @Test
    public void toObjectId_validHex_returnsObjectId() {
        final var id = new ObjectId();
        final var result = converters.toObjectId(id.toHexString());
        assertEquals(result, id);
    }

    @Test
    public void toObjectId_emptyString_returnsNull() {
        // Previously threw IllegalArgumentException("state should be: hexString has 24 characters")
        assertNull(converters.toObjectId(""));
    }

    @Test
    public void toObjectId_tooShort_returnsNull() {
        assertNull(converters.toObjectId("short"));
    }

    @Test
    public void toObjectId_24NonHexChars_returnsNull() {
        // Exactly 24 characters but not all hex — not a valid ObjectId
        assertNull(converters.toObjectId("zzzzzzzzzzzzzzzzzzzzzzzz"));
    }

    @Test
    public void toObjectId_23HexChars_returnsNull() {
        // One char short of a valid ObjectId
        assertNull(converters.toObjectId("507f1f77bcf86cd7994390"));
    }

    @Test
    public void toObjectId_25HexChars_returnsNull() {
        // One char over a valid ObjectId
        assertNull(converters.toObjectId("507f1f77bcf86cd799439011a"));
    }

}
