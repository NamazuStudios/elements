/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.namazustudios.socialengine.jnlua;

import java.util.function.Supplier;

/**
 * Provides proxy access to a Lua value from Java. Lua value proxies are
 * acquired by invoking one of the <code>getProxy()</code> methods on the Lua
 * state.
 * 
 * @see LuaState#getProxy(int)
 * @see LuaState#getProxy(int, Class)
 * @see LuaState#getProxy(int, Class[])
 */
public interface LuaValueProxy {
	/**
	 * Returns the Lua state of this proxy.
	 * 
	 * @return the Lua state
	 */
	LuaState getLuaState();

	/**
	 * Pushes the proxied Lua value on the stack of the Lua state.
	 */
	void pushValue();

	/**
	 * {@see {@link LuaState#doInLock(Runnable)}}
	 *
	 * @param runnable the task to perform
	 */
	default void doInLock(final Runnable runnable) {
		getLuaState().doInLock(runnable);
	}

	/**
	 * {@see {@link LuaState#doInLock(Runnable)}}
	 *
	 * @param task the task to perform
	 * @param <T> the return type
	 * @return the value returend by the supplied task
 	 */
	default <T> T computeInLock(final Supplier<T> task) {
		return getLuaState().computeInLock(task);
	}

}
