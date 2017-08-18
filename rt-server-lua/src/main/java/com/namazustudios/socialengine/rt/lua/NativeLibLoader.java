package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.NativeSupport;
import org.scijava.nativelib.NativeLibraryUtil;
import org.scijava.nativelib.NativeLoader;

/**
 * Implementation of {@link com.naef.jnlua.NativeSupport.Loader} which unpacks the
 * native libraries and loads it using {@link NativeLoader}.
 *
 * Created by patricktwohig on 8/17/17.
 */
public class NativeLibLoader implements NativeSupport.Loader {
    @Override
    public void load() {
        NativeLibraryUtil.loadNativeLibrary(NativeLibLoader.class, "jnlua");
    }
}
