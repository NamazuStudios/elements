/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.namazustudios.socialengine.jnlua;

/**
 * Loads the JNLua native library.
 * 
 * The class provides and configures a default loader implementation that loads
 * the JNLua native library by means of the <code>System.loadLibrary</code>
 * method. In some situations, you may want to override this behavior. For
 * example, when using JNLua as an OSGi bundle, the native library is loaded by
 * the OSGi runtime. Therefore, the OSGi bundle activator replaces the loader by
 * a no-op implementaion. Note that the loader must be configured before
 * LuaState is accessed.
 */
public final class NativeSupport {

	private static final NativeSupport instance = new NativeSupport();

	private Loader loader = DefaultLoader.getInstance();

	/**
	 * Returns the instance.
	 *
	 * @return the instance
	 */
	public static NativeSupport getInstance() {
		return instance;
	}

	/**
	 * Private constructor to prevent external instantiation.
	 */
	private NativeSupport() { }

	/**
	 * Return the native library loader.
	 *
	 * @return the loader
	 */
	public Loader getLoader() {
		return loader;
	}

	/**
	 * Sets the native library loader.
	 *
	 * @param loader
	 *            the loader
	 */
	public void setLoader(Loader loader) {
		if (loader == null) {
			throw new NullPointerException("loader must not be null");
		}
		this.loader = loader;
	}

}
