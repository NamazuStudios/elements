/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.namazustudios.socialengine.jnlua;

/**
 * Provides a named Java function.
 */
public interface NamedJavaFunction extends JavaFunction {
	/**
	 * Returns the name of this Java function.
	 * 
	 * @return the Java function name
	 */
	public String getName();
}
