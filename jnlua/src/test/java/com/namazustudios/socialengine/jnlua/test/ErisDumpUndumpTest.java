package com.namazustudios.socialengine.jnlua.test;

import com.namazustudios.socialengine.jnlua.JavaModule;
import com.namazustudios.socialengine.jnlua.LuaState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;

import static com.namazustudios.socialengine.jnlua.LuaState.Library.JAVA;
import static org.junit.Assert.*;

public class ErisDumpUndumpTest {

    private static final Logger logger = LoggerFactory.getLogger(ErisDumpUndumpTest.class);

    private LuaState luaState;

    @Before
    public void setup() {
        luaState = new LuaState();
    }

    @After
    public void teardown() {
        try {
            luaState.close();
        } catch (Exception e) {
            logger.error("Failed to close Lua State", e);
            fail("Failed to close Lua State.");
        }
    }

    @Test
    public void testDumpUndump() throws IOException {

        // This test isn't very comprehensive mostly because Eris provides its own tests.  This just ensures
        // that the API exposed through it is properly implemented and that serialization actually happens as expected.

        luaState.newTable(); // Perms, empty table
        luaState.newTable(); // Value to persist.

        luaState.pushString("Hello");
        luaState.setField(-2, "Hello");

        luaState.pushString("World");
        luaState.setField(-2, "World");

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            luaState.persist(bos, 1, 2);
            bytes = bos.toByteArray();
        }

        // Ensure that bytes were actually written to the stream.
        assertTrue(bytes.length > 0);

        // We need to ensure that the operation did not affect the stack.  We also pop the stack to zero to ensure that
        // the existing values do not affect the subsequent read and we indeed read in a fresh new value.

        assertEquals(2, luaState.getTop());
        luaState.pop(2);

        luaState.newTable(); // Perms, empty table

        try (final InputStream is = new ByteArrayInputStream(bytes)) {
            luaState.unpersist(is, 1);
        }

        // Ensure that the result was pushed on the stack as advertised.
        assertEquals(2, luaState.getTop());

        // Finally ensure that the values we expected are there.
        luaState.getField(-1, "Hello");
        assertEquals("Hello", luaState.toString(-1));
        luaState.pop(1);

        luaState.getField(-1, "World");
        assertEquals("World", luaState.toString(-1));
        luaState.pop(1);

        luaState.pop(luaState.getTop());
        assertEquals(0, luaState.getTop());

    }

    @Test
    public void testSystemPermsDoesNotContainJavaIfNotLoaded() {

        luaState.pushSystemPermanents();

        luaState.pushNil();

        while (luaState.next(1)) {
            assertTrue("Expecting String.  Got " + luaState.typeName(-1), luaState.isString(-1));
            assertFalse(JavaModule.class.getName().equals(luaState.toString(-1)));
            luaState.pop(1);
        }

        luaState.pop(1);

        luaState.pushSystemInversePermanents();
        luaState.getField(1, JavaModule.class.getName());
        assertTrue(luaState.isNil(-1));
        luaState.pop(1);

    }

    @Test
    public void testSystemPermsDoesContainsJavaIfLoaded() {

        luaState.openLib(JAVA);
        luaState.pushSystemPermanents();

        luaState.pushValue(1);
        luaState.getTable(-2);
        assertFalse(luaState.isNil(-1));

        luaState.pop(2);

        luaState.pushSystemInversePermanents();
        luaState.getField(1, JavaModule.class.getName());
        assertTrue(luaState.isNil(-1));
        luaState.pop(1);

    }

}
