/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.namazustudios.socialengine.jnlua;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an execution point in a Lua stack trace.
 */
public class LuaStackTraceElement implements Serializable {

	private static final String MAIN = "main";

	// -- State
	private String what;
	private String functionName;
	private String sourceName;
	private int lineNumber;

	public LuaStackTraceElement() {}

	// -- Construction
	/**
	 * Creates a new instance.
	 *
	 * @param what the "what" value which caused the excepteion
	 * @param functionName
	 *            the function name, or <code>null</code> if unavailable
	 * @param sourceName
 *            the source name, or <code>null</code> if unavailable
	 * @param lineNumber
	 */
	public LuaStackTraceElement(final String what, final String functionName, final String sourceName, final int lineNumber) {
		this.what = what;
		this.functionName = (functionName == null && MAIN.equals(what)) ? what : functionName;
		this.sourceName = sourceName;
		this.lineNumber = lineNumber;
	}

	// -- Properties
	/**
	 * Returns the name of the function containing the execution point
	 * represented by this stack trace element. If there is no function name for
	 * the execution point, the method returns <code>null</code>.
	 * 
	 * @return the name of the function containing the execution point
	 *         represented by this stack trace element, or <code>null</code>
	 */
	public String getFunctionName() {
		return functionName;
	}

	/**
	 * Returns the name of the source containing the execution point represented
	 * by this this stack trace element. The source name is passed to the Lua
	 * state when the Lua source code is loaded. If there is no source name for
	 * the execution point, the method returns <code>null</code>.
	 * 
	 * @return the source name, or <code>null</code>
	 * @see LuaState#load(java.io.InputStream, String, String)
	 * @see LuaState#load(String, String)
	 */
	public String getSourceName() {
		return sourceName;
	}

	/**
	 * Returns the line number in the source containing the execution point
	 * represented by this stack trace element. If there is no line number for
	 * the execution point, the method returns a negative number.
	 * 
	 * @return the line number, or a negative number if there is no line number
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof LuaStackTraceElement)) return false;
		LuaStackTraceElement that = (LuaStackTraceElement) object;
		return getLineNumber() == that.getLineNumber() &&
				Objects.equals(what, that.what) &&
				Objects.equals(getFunctionName(), that.getFunctionName()) &&
				Objects.equals(getSourceName(), that.getSourceName());
	}

	@Override
	public int hashCode() {

		return Objects.hash(what, getFunctionName(), getSourceName(), getLineNumber());
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(what).append(":");

		if (functionName != null) {
			sb.append(functionName);
		} else {
			sb.append("(Unknown Function)");
		}
		sb.append(" (");
		if (sourceName != null) {
			sb.append(sourceName);
			if (lineNumber >= 0) {
				sb.append(':');
				sb.append(lineNumber);
			}
		} else {
			sb.append("External Function");
		}
		sb.append(')');
		return sb.toString();
	}

	// -- Private methods
	/**
	 * Returns whether two objects are equal, handling <code>null</code>.
	 */
	private boolean safeEquals(Object a, Object b) {
		return a == b || a != null && a.equals(b);
	}
}
