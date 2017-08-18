package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.NativeSupport;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.scijava.nativelib.NativeLoader;

import java.io.IOException;

import static org.scijava.nativelib.NativeLoader.loadLibrary;

/**
 * Implementation of {@link com.naef.jnlua.NativeSupport.Loader} which unpacks the
 * native libraries and loads it using {@link NativeLoader}.
 *
 * Created by patricktwohig on 8/17/17.
 */
public class NativeLibLoader implements NativeSupport.Loader {
    @Override
    public void load() {
        try {
            loadLibrary("jnlua");
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }
}
