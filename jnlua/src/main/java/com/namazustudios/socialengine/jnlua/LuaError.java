/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.namazustudios.socialengine.jnlua;

import java.io.Serializable;

/**
 * Contains information about a Lua error condition. This object is created in
 * the native library.
 */
public class LuaError implements Serializable {

	// -- State

	private String message;

	private Throwable cause;

	private LuaStackTraceElement[] luaStackTrace;

	// -- Properties

	/**
	 * Returns the message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the Lua stack trace.
	 */
	public LuaStackTraceElement[] getLuaStackTrace() {
		return luaStackTrace;
	}

	/**
	 * Returns the cause.
	 */
	public Throwable getCause() {
		return cause;
	}

	// -- Object methods
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		if (message != null) sb.append(message);
		if (cause != null)  sb.append(cause);
		return sb.toString();
	}

}
