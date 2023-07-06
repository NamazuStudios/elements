/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.namazustudios.socialengine.jnlua.util;

import java.util.*;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.jnlua.LuaValueProxy;

/**
 * Abstract map implementation backed by a Lua table.
 */
public abstract class AbstractTableMap<K> extends AbstractMap<K, Object> implements LuaValueProxy {

	// -- State
	private Set<Map.Entry<K, Object>> entrySet;

	// -- Construction
	/**
	 * Creates a new instance.
	 */
	public AbstractTableMap() {}

	// -- Map methods
	@Override
	public Set<Map.Entry<K, Object>> entrySet() {

		if (entrySet == null) {
			entrySet = new EntrySet();
		}

		return entrySet;

	}

	@Override
	public boolean isEmpty() {
		return entrySet().isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {

		final var luaState = getLuaState();

		return luaState.computeInLock(() -> {

			checkKey(key);

			pushValue();

			luaState.pushJavaObject(key);
			luaState.getTable(-2);

			try {
				return !luaState.isNil(-1);
			} finally {
				luaState.pop(2);
			}

		});

	}

	@Override
	public Object get(final Object key) {

		final var luaState = getLuaState();

		return computeInLock(() -> {

			checkKey(key);

			pushValue();

			luaState.pushJavaObject(key);
			luaState.getTable(-2);

			try {
				return luaState.toJavaObject(-1, Object.class);
			} finally {
				luaState.pop(2);
			}

		});

	}

	@Override
	public Object put(final K key, final Object value) {

		final var luaState = getLuaState();

		return computeInLock(() -> {

			checkKey(key);

			final var oldValue = get(key);

			pushValue();
			luaState.pushJavaObject(key);
			luaState.pushJavaObject(value);
			luaState.setTable(-3);
			luaState.pop(1);

			return oldValue;

		});
	}

	@Override
	public Object remove(Object key) {

		final var luaState = getLuaState();

		return computeInLock(() -> {

			checkKey(key);

			final var oldValue = get(key);

			pushValue();
			luaState.pushJavaObject(key);
			luaState.pushNil();
			luaState.setTable(-3);
			luaState.pop(1);

			return oldValue;

		});

	}

	// -- Protected methods
	/**
	 * Checks a key for validity. If the key is not valid, the method throws an
	 * appropriate runtime exception. The method is invoked for all input keys.
	 * 
	 * <p>
	 * This implementation checks that the key is not <code>null</code>. Lua
	 * does not allow <code>nil</code> as a table key. Subclasses may implement
	 * more restrictive checks.
	 * </p>
	 * 
	 * @param key
	 *            the key
	 * @throws NullPointerException
	 *             if the key is <code>null</code>
	 */
	protected void checkKey(final Object key) {
		if (key == null) throw new NullPointerException("key must not be null");
	}

	/**
	 * Indicates if this table map filters keys from the Lua table. If the
	 * method returns <code>true</code>, the table map invokes
	 * {@link #acceptKey(int)} on each key retrieved from the underlying table
	 * to determine whether the key is accepted or rejected.
	 * 
	 * <p>
	 * This implementation returns <code>false</code>. Subclasses may override
	 * the method alongside {@link #acceptKey(int)} to implement key filtering.
	 * </p>
	 * 
	 * @return whether this table map filters keys from the Lua table
	 */
	protected boolean filterKeys() {
		return false;
	}

	/**
	 * Accepts or rejects a key from the Lua table. Only table keys that are
	 * accepted are processed. The method allows subclasses to filter the Lua
	 * table. The method is called only if {@link #filterKeys()} returns
	 * <code>true</code>.
	 * 
	 * <p>
	 * This implementation returns <code>true</code> regardless of the input,
	 * thus accepting all keys. Subclasses may override the method alongside
	 * {@link #filterKeys()} to implement key filtering.
	 * </p>
	 * 
	 * @param index
	 *            the stack index containing the candidate key
	 * @return whether the key is accepted
	 */
	protected boolean acceptKey(int index) {
		return true;
	}

	/**
	 * Converts the key at the specified stack index to a Java object. If this
	 * table maps performs key filtering, the method is invoked only for keys it
	 * has accepted.
	 * 
	 * @param index
	 *            the stack index containing the key
	 * @return the Java object representing the key
	 * @see #filterKeys()
	 * @see #acceptKey(int)
	 */
	protected abstract K convertKey(int index);

	// -- Nested types
	/**
	 * Lua table entry set.
	 */
	private class EntrySet extends AbstractSet<Map.Entry<K, Object>> {
		// -- Set methods
		@Override
		public Iterator<Map.Entry<K, Object>> iterator() {
			return new EntryIterator();
		}

		@Override
		public boolean isEmpty() {
			return computeInLock(() -> {

				final var luaState = getLuaState();

				pushValue();
				luaState.pushNil();

				while (luaState.next(-2)) {
					if (!filterKeys() || acceptKey(-2)) {
						luaState.pop(3);
						return false;
					}
				}

				luaState.pop(1);
				return true;

			});
		}

		@Override
		public int size() {
			return computeInLock(() -> {

				final var luaState = getLuaState();

				int count = 0;
				pushValue();

				if (filterKeys()) {
					luaState.pushNil();
					while (luaState.next(-2)) {
						if (acceptKey(-2)) {
							count++;
						}
						luaState.pop(1);
					}
				} else {
					count = luaState.tableSize(-1);
				}

				luaState.pop(1);
				return count;

			});

		}

		@Override
		public boolean contains(Object object) {

			checkKey(object);

			if (!(object instanceof AbstractTableMap<?>.Entry)) {
				return false;
			}

			@SuppressWarnings("unchecked")
			final var luaTableEntry = (Entry) object;
			if (luaTableEntry.getLuaState() != getLuaState()) {
				return false;
			}

			return containsKey(luaTableEntry.key);

		}

		@Override
		public boolean remove(Object object) {

			if (!(object instanceof AbstractTableMap<?>.Entry)) {
				return false;
			}

			@SuppressWarnings("unchecked")
			Entry luaTableEntry = (Entry) object;

			if (luaTableEntry.getLuaState() != getLuaState()) {
				return false;
			}

			return getLuaState().computeInLock(() -> {

				final var luaState = getLuaState();

				pushValue();
				luaState.pushJavaObject(object);
				luaState.getTable(-2);

				boolean contains = !luaState.isNil(-1);
				luaState.pop(1);

				if (contains) {
					luaState.pushJavaObject(object);
					luaState.pushNil();
					luaState.setTable(-3);
				}
				luaState.pop(1);

				return contains;

			});

		}
	}

	/**
	 * Lua table iterator.
	 */
	private class EntryIterator implements Iterator<Map.Entry<K, Object>> {
		// -- State
		private K key;

		// -- Iterator methods
		@Override
		public boolean hasNext() {
			return computeInLock(() -> {

				final var luaState = getLuaState();
				pushValue();

				luaState.pushJavaObject(key);
				while (luaState.next(-2)) {
					if (!filterKeys() || acceptKey(-2)) {
						luaState.pop(3);
						return true;
					}
				}

				luaState.pop(1);
				return false;

			});

		}

		@Override
		public Map.Entry<K, Object> next() {
			return computeInLock(() -> {

				final var luaState = getLuaState();

				pushValue();
				luaState.pushJavaObject(key);
				while (luaState.next(-2)) {
					if (!filterKeys() || acceptKey(-2)) {
						key = convertKey(-2);
						luaState.pop(3);
						return new Entry(key);
					}
				}

				luaState.pop(1);
				throw new NoSuchElementException();

			});


		}

		/*** Temporary code ***/
		public List<String> getStack() {


			final LuaState luaState = getLuaState();
			if (luaState == null) return Collections.emptyList();

			final List<String> stack = new ArrayList<>();
			walkCopyOfVMStack(luaState, i -> stack.add(luaState.type(i) + " - " + luaState.toString(i)));
			return stack;

		}

		private void walkCopyOfVMStack(final LuaState luaState, final IntConsumer indexConsumer) {

			final int top = luaState.getTop();

			for (int i = 1; i <= top; ++i) {
				luaState.pushValue(i);
			}

			final int newTop = luaState.getTop();

			try {
				IntStream.range(top, newTop).forEach(indexConsumer);
			} finally {
				luaState.pop(newTop - top);
			}

		}

		/*** Temporary code ***/

		@Override
		public void remove() {
			doInLock(() -> {
				final var luaState = getLuaState();
				pushValue();
				luaState.pushJavaObject(key);
				luaState.pushNil();
				luaState.setTable(-3);
				luaState.pop(1);
			});
		}

	}

	/**
	 * Bindings entry.
	 */
	private class Entry implements Map.Entry<K, Object> {
		// -- State
		private K key;

		// -- Construction
		/**
		 * Creates a new instance.
		 */
		public Entry(K key) {
			this.key = key;
		}

		// -- Map.Entry methods
		@Override
		public K getKey() {
			return key;
		}

		@Override
		public Object getValue() {
			return get(key);
		}

		@Override
		public Object setValue(Object value) {
			return put(key, value);
		}

		// -- Object methods
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof AbstractTableMap<?>.Entry)) {
				return false;
			}
			@SuppressWarnings("unchecked")
			Entry other = (Entry) obj;
			return getLuaState() == other.getLuaState()
					&& key.equals(other.key);
		}

		@Override
		public int hashCode() {
			return getLuaState().hashCode() * 65599 + key.hashCode();
		}

		@Override
		public String toString() {
			return key.toString();
		}

		// -- Private methods
		/**
		 * Returns the Lua script engine.
		 */
		private LuaState getLuaState() {
			return AbstractTableMap.this.getLuaState();
		}
	}
}