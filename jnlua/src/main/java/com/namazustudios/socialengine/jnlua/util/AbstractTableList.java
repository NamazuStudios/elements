/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.namazustudios.socialengine.jnlua.util;

import java.util.AbstractList;
import java.util.RandomAccess;

import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.jnlua.LuaValueProxy;

/**
 * Abstract list implementation backed by a Lua table.
 */
public abstract class AbstractTableList extends AbstractList<Object> implements
		RandomAccess, LuaValueProxy {
	// -- Construction
	/**
	 * Creates a new instance.
	 */
	public AbstractTableList() {
	}

	// -- List methods
	@Override
	public void add(int index, Object element) {
		doInLock(() -> {

			final var luaState = getLuaState();

			final int size = size();

			if (index < 0 || index > size)
				throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);

			pushValue();
			luaState.tableMove(-1, index + 1, index + 2, size - index);
			luaState.pushJavaObject(element);
			luaState.rawSet(-2, index + 1);
			luaState.pop(1);

		});
	}

	@Override
	public Object get(int index) {
		return computeInLock(() -> {

			final var luaState = getLuaState();

			int size = size();
			if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);

			pushValue();
			luaState.rawGet(-1, index + 1);

			try {
				return luaState.toJavaObject(-1, Object.class);
			} finally {
				luaState.pop(2);
			}

		});
	}

	@Override
	public Object remove(int index) {
		return computeInLock(() -> {

			final var luaState = getLuaState();

			int size = size();
			if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);

			final var oldValue = get(index);
			pushValue();
			luaState.tableMove(-1, index + 2, index + 1, size - index - 1);
			luaState.pushNil();
			luaState.rawSet(-2, size);
			luaState.pop(1);

			return oldValue;

		});
	}

	@Override
	public Object set(int index, Object element) {
		return computeInLock(() -> {

			final var luaState = getLuaState();

			int size = size();
			if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);

			final var oldValue = get(index);
			pushValue();
			luaState.pushJavaObject(element);
			luaState.rawSet(-2, index + 1);
			luaState.pop(1);

			return oldValue;

		});
	}

	@Override
	public int size() {
		return computeInLock(() -> {

			pushValue();
			final var luaState = getLuaState();

			try {
				return luaState.rawLen(-1);
			} finally {
				luaState.pop(1);
			}

		});
	}

}
