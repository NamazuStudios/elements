package com.namazustudios.socialengine;

import java.util.Properties;
import java.util.function.Supplier;

/**
 * A type of {@link Supplier} used to provide defaults for a particular module.
 *
 * Created by patricktwohig on 5/25/17.
 */
public interface ModuleDefaults extends Supplier<Properties> {}
