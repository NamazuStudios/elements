/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.namazustudios.socialengine.jnlua;

import java.io.PrintStream;
import java.io.PrintWriter;

import static java.lang.System.arraycopy;

/**
 * Indicates a Lua runtime error.
 * 
 * <p>
 * This exception is thrown if a Lua runtime error occurs. The class provides
 * access to the Lua stack trace by means of the {@link #getLuaStackTrace()}
 * method.
 * </p>
 */
public class LuaRuntimeException extends LuaException {

	private static final String UNKNOWN = "<unknown>";

	private static final String LUA_DUMMY_CLASS = "Lua";

	// -- Static
	private static final long serialVersionUID = 1L;
	private static final LuaStackTraceElement[] EMPTY_LUA_STACK_TRACE = new LuaStackTraceElement[0];

	// -- State
	private LuaStackTraceElement[] luaStackTrace;

	// -- Construction

	/**
	 * Default Constructuor.
	 */
	public LuaRuntimeException() {
		this.luaStackTrace = EMPTY_LUA_STACK_TRACE;
	}

	/**
	 * Creates a new instance. The instance is created with an empty Lua stack
	 * trace.
	 * 
	 * @param msg
	 *            the message
	 */
	public LuaRuntimeException(String msg) {
		super(msg);
		luaStackTrace = EMPTY_LUA_STACK_TRACE;
	}

	/**
	 * Creates a new instance. The instance is created with an empty Lua stack
	 * trace.
	 * 
	 * @param msg
	 *            the message
	 * @param cause
	 *            the cause of this exception
	 */
	public LuaRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
		luaStackTrace = EMPTY_LUA_STACK_TRACE;
	}

	/**
	 * Creates a new instance. The instance is created with an empty Lua stack
	 * trace.
	 * 
	 * @param cause
	 *            the cause of this exception
	 */
	public LuaRuntimeException(Throwable cause) {
		super(cause);
		luaStackTrace = EMPTY_LUA_STACK_TRACE;
	}

	// -- Properties
	/**
	 * Returns the Lua stack trace of this runtime exception.
	 */
	public LuaStackTraceElement[] getLuaStackTrace() {
		return luaStackTrace.clone();
	}

	// -- Operations
	/**
	 * Prints this exception and its Lua stack trace to the standard error
	 * stream.
	 */
	public void printLuaStackTrace() {
		printLuaStackTrace(System.err);
	}

	/**
	 * Prints this exception and its Lua stack trace to the specified print
	 * stream.
	 * 
	 * @param s
	 *            the print stream
	 */
	public void printLuaStackTrace(PrintStream s) {
		synchronized (s) {

			s.println(this);
			for (int i = 0; i < luaStackTrace.length; i++) {
				s.println("\tat " + luaStackTrace[i]);
			}

			super.printStackTrace();

		}
	}

	/**
	 * Prints this exception and its Lua stack trace to the specified print
	 * writer.
	 * 
	 * @param s
	 *            the print writer
	 */
	public void printLuaStackTrace(PrintWriter s) {
		synchronized (s) {
			s.println(this);
			for (int i = 0; i < luaStackTrace.length; i++) {
				s.println("\tat " + luaStackTrace[i]);
			}
		}
	}

	// -- Package private methods
	/**
	 * Sets the Lua error in this exception. The method in invoked from the
	 * native library.
	 */
	void setLuaError(LuaError luaError) {
		initCause(luaError.getCause());
		luaStackTrace = luaError.getLuaStackTrace();

		final StackTraceElement[] original = getStackTrace();
		final StackTraceElement[] replacement = new StackTraceElement[luaStackTrace.length + original.length];

		for (int i = 0; i < luaStackTrace.length; ++i) {

			final LuaStackTraceElement luaStackTraceElement = luaStackTrace[i];

			final String functionName = luaStackTraceElement.getFunctionName();
			final String sourceName = luaStackTraceElement.getSourceName();

			replacement[i] = new StackTraceElement(
				LUA_DUMMY_CLASS,
				functionName == null ? UNKNOWN : functionName,
				sourceName == null ? UNKNOWN : sourceName,
				luaStackTraceElement.getLineNumber()
			);

		}

		arraycopy(original, 0, replacement, luaStackTrace.length, original.length);
		setStackTrace(replacement);

	}

}
