/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.namazustudios.socialengine.jnlua.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.namazustudios.socialengine.jnlua.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * Contains unit tests for Lua exceptions.
 */
public class LuaExceptionTest extends AbstractLuaTest {
	// -- Test cases
	/**
	 * Tests the call of a Lua function which invokes the Lua error function.
	 */
	@Test
	public void testLuaError() throws Exception {
		// Load program
		luaState.openLibs();
		final String code =
			// language=Lua
			"function A ()\n" +
			"    B()\n" +
			"end\n" +
			"\n" +
			"function B ()\n" +
			"    C()\n" +
			"end\n" +
			"\n" +
			"function C ()\n" +
			"    error(\"msg\")\n" +
			"end\n" +
			"\n" +
			"A()\n";

		luaState.load(code, "=testLuaError");

		// Run
		LuaRuntimeException luaRuntimeException = null;
		try {
			luaState.call(0, 0);
		} catch (LuaRuntimeException e) {
			luaRuntimeException = e;
		}
		assertTrue(luaRuntimeException.getMessage().endsWith("msg"));
		LuaStackTraceElement[] luaStackTrace = luaRuntimeException
				.getLuaStackTrace();
		assertEquals(6, luaStackTrace.length);
		assertEquals(new LuaStackTraceElement("C", null, "[C]", -1),
				luaStackTrace[0]);
		assertEquals(new LuaStackTraceElement("C", "error", "[C]", -1),
				luaStackTrace[1]);
		assertEquals(new LuaStackTraceElement("Lua", "C", "testLuaError", 10),
				luaStackTrace[2]);
		assertEquals(new LuaStackTraceElement("Lua", "B", "testLuaError", 6), luaStackTrace[3]);
		assertEquals(new LuaStackTraceElement("Lua", "A", "testLuaError", 2), luaStackTrace[4]);
		assertEquals(new LuaStackTraceElement("main", null, "testLuaError", 13),
				luaStackTrace[5]);
	}

	/**
	 * Tests the call of a Java function which throws a Java runtime exception.
	 */
	@Test
	public void testRuntimeException() throws Exception {
		// Push function
		luaState.pushJavaFunction(new RuntimeExceptionFunction());

		// Push arguments
		LuaRuntimeException luaRuntimeException = null;
		try {
			luaState.call(0, 0);
		} catch (LuaRuntimeException e) {
			luaRuntimeException = e;
		}
		assertNotNull(luaRuntimeException);
		Throwable cause = luaRuntimeException.getCause();
		assertNotNull(cause);
		assertTrue(cause instanceof ArithmeticException);
	}

	/**
	 * Tests the call of a Java function which throws a Lua runtime exception.
	 */
	@Test
	public void testLuaRuntimeException() throws Exception {
		// Push function
		luaState.pushJavaFunction(new LuaRuntimeExceptionFunction());

		// Push arguments
		LuaRuntimeException luaRuntimeException = null;
		try {
			luaState.call(0, 0);
		} catch (LuaRuntimeException e) {
			luaRuntimeException = e;
		}
		assertNotNull(luaRuntimeException);
		Throwable cause = luaRuntimeException.getCause();
		assertNotNull(cause);
		assertTrue(cause instanceof LuaRuntimeException);
	}

	/**
	 * Tests the generation of a Lua syntax exception on Lua code with invalid
	 * syntax.
	 */
	@Test
	public void testLuaSyntaxException() throws Exception {
		LuaSyntaxException luaSyntaxException = null;
		try {
			luaState.load("An invalid chunk of Lua.", "=testLuaSyntaxException");
		} catch (LuaSyntaxException e) {
			luaSyntaxException = e;
		}
		assertNotNull(luaSyntaxException);
	}

	/**
	 * Tests the generation of a Lua GC metamethod exception on a Lua value
	 * raising an error in its <code>__gc</code> metamethod.
	 */
	@Test
	public void testLuaGcMetamethodException() throws Exception {
		LuaGcMetamethodException luaGcMetamethodException = null;
		luaState.openLib(LuaState.Library.BASE);
		luaState.pop(1);
		luaState.load(
				"setmetatable({}, { __gc = function() error(\"gc\") end })\n"
						+ "collectgarbage()", "=testLuaGcMetamethodException");
		try {
			luaState.call(0, 0);
		} catch (LuaGcMetamethodException e) {
			luaGcMetamethodException = e;
		}
		assertNotNull(luaGcMetamethodException);
	}

	@Test(expected = LuaRuntimeException.class)
	public void testThreadNativeError() throws Exception {

		// Create thread
		luaState.openLibs();
		luaState.register(new NamedJavaFunction() {
			public int invoke(LuaState luaState) {
				luaState.pushInteger(luaState.toInteger(1));
				return luaState.yield(1);
			}

			public String getName() {
				return "yieldfunc";
			}
		});

		luaState.register(new NamedJavaFunction() {
			public int invoke(LuaState luaState) {
				throw new IllegalStateException("Illegal State!");
			}

			public String getName() {
				return "throwfunc";
			}
		});

		final String chunk =
			// language=Lua
			"yieldfunc(1)\n" +
			"throwfunc()";

		luaState.load(chunk, "=testThread");
		luaState.newThread();
		Assert.assertEquals(LuaType.THREAD, luaState.type(-1));

		// Start
		luaState.pushInteger(1);
		Assert.assertEquals(1, luaState.resume(1, 1));
		Assert.assertEquals(LuaState.YIELD, luaState.status(1));
		Assert.assertEquals(2, luaState.getTop());
		Assert.assertEquals(1, luaState.toInteger(-1));
		luaState.pop(1);

		// Resume, should fail with LuaRuntimeException
		try {
			luaState.resume(1, 0);
			Assert.fail("Expected exception not thrown.");
		} catch (LuaRuntimeException ex) {
			assertTrue(ex.getCause() instanceof IllegalStateException);
			assertEquals("Illegal State!", ex.getCause().getMessage());
			throw ex;
		}

	}

	@Test
	public void testDeepStackThreadError() throws Exception {
		// Load program
		luaState.openLibs();
		final String code =
			// language=Lua
			"function A ()\n" +
			"    B()\n" +
			"end\n" +
			"\n" +
			"function B ()\n" +
			"    C()\n" +
			"end\n" +
			"\n" +
			"function C ()\n" +
			"    error(\"msg\")\n" +
			"end\n" +
			"\n" +
			"A()\n";

		luaState.load(code, "=testDeepStackThreadError");
		luaState.newThread();
		Assert.assertEquals(LuaType.THREAD, luaState.type(-1));

		// Run
		LuaRuntimeException luaRuntimeException = null;
		try {
			luaState.resume(1, 0);
			Assert.fail("Expected exception not thrown.");
		} catch (LuaRuntimeException e) {
			luaRuntimeException = e;
		}

		assertTrue(luaRuntimeException.getMessage().endsWith("msg"));
		LuaStackTraceElement[] luaStackTrace = luaRuntimeException
				.getLuaStackTrace();
		assertEquals(5, luaStackTrace.length);
		assertEquals(new LuaStackTraceElement("C", "error", "[C]", -1), luaStackTrace[0]);
		assertEquals(new LuaStackTraceElement("Lua", "C", "testDeepStackThreadError", 10), luaStackTrace[1]);
		assertEquals(new LuaStackTraceElement("Lua", "B", "testDeepStackThreadError", 6), luaStackTrace[2]);
		assertEquals(new LuaStackTraceElement("Lua", "A", "testDeepStackThreadError", 2), luaStackTrace[3]);
		assertEquals(new LuaStackTraceElement("main", null, "testDeepStackThreadError", 13),
				luaStackTrace[4]);
	}

	@Test
	public void testDeepStackThreadErrorJava() throws Exception {
		// Load program
		luaState.openLibs();

		luaState.register(new NamedJavaFunction() {
			public int invoke(LuaState luaState) {
				throw new IllegalStateException("Illegal State!");
			}

			public String getName() {
				return "C";
			}

		});

		final String code =
			// language=Lua
			"function A ()\n" +
			"    B()\n" +
			"end\n" +
			"\n" +
			"function B ()\n" +
			"    C()\n" +
			"end\n" +
			"\n" +
			"A()\n";

		luaState.load(code, "=testDeepStackThreadErrorJava");
		luaState.newThread();
		Assert.assertEquals(LuaType.THREAD, luaState.type(-1));

		// Run
		LuaRuntimeException luaRuntimeException = null;
		try {
			luaState.resume(1, 0);
			Assert.fail("Expected exception not thrown.");
		} catch (LuaRuntimeException e) {
			luaRuntimeException = e;
		}

		LuaStackTraceElement[] luaStackTrace = luaRuntimeException
				.getLuaStackTrace();
		assertEquals(4, luaStackTrace.length);
		assertEquals(new LuaStackTraceElement("C", "C", "[C]", -1), luaStackTrace[0]);
		assertEquals(new LuaStackTraceElement("Lua", "B", "testDeepStackThreadErrorJava", 6), luaStackTrace[1]);
		assertEquals(new LuaStackTraceElement("Lua", "A", "testDeepStackThreadErrorJava", 2), luaStackTrace[2]);
		assertEquals(new LuaStackTraceElement("main", null, "testDeepStackThreadErrorJava", 9),
				luaStackTrace[3]);
	}
	// -- Private classes
	/**
	 * Provides a function throwing a Java runtime exception.
	 */
	private class RuntimeExceptionFunction implements JavaFunction {
		public int invoke(LuaState luaState) throws LuaRuntimeException {
			@SuppressWarnings("unused")
			int a = 0 / 0;
			return 0;
		}
	}
	
	/**
	 * Provides a function throwing a Lua runtime exception with a cause.
	 */
	private class LuaRuntimeExceptionFunction implements JavaFunction {
		public int invoke(LuaState luaState) throws LuaRuntimeException {
			try {
				@SuppressWarnings("unused")
				int a = 0 / 0;
			} catch (ArithmeticException e) {
				throw new LuaRuntimeException(e.getMessage(), e);
			}
			return 0;
		}
	}
}
