/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.namazustudios.socialengine.jnlua.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.namazustudios.socialengine.jnlua.JavaModule;
import org.junit.Test;

/**
 * Contains unit tests for the Java module.
 */
public class JavaModuleTest extends AbstractLuaTest {
	// ---- Test cases
	/**
	 * Tests the toTable method.
	 */
	@Test
	public void testToTable() {
		// Map
		Map<Object, Object> map = new HashMap<Object, Object>();
		luaState.pushJavaObject(JavaModule.getInstance().toTable(map));
		luaState.setGlobal("map");
		luaState.load("map.x = 1", "=testToTable");
		luaState.call(0, 0);
		assertEquals(Integer.valueOf(1), map.get("x"));

		// List
		List<Object> list = new ArrayList<Object>();
		luaState.pushJavaObject(JavaModule.getInstance().toTable(list));
		luaState.setGlobal("list");
		luaState.load("list[1] = 1", "=testToList");
		luaState.call(0, 0);
		assertEquals(Integer.valueOf(1), list.get(0));
	}
	
	/**
	 * Tests the Java module from Lua.
	 */
	@Test
	public void testJavaModule() throws Exception {
		runTest("com/namazustudios/socialengine/jnlua/test/JavaModule.lua", "JavaModule");
	}
}
