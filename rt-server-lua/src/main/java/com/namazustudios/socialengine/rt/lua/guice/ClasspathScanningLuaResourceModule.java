package com.namazustudios.socialengine.rt.lua.guice;

import com.google.common.base.Predicate;
import com.google.common.reflect.ClassPath;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Ref;
import java.util.regex.Pattern;

/**
 * Scans the classpath, attempting to add all *lua files in a particular package.
 *
 * Usage of this module requires the {@link Reflections} library on the classpath,
 * which is an optional dependency.
 *
 * Created by patricktwohig on 9/2/15.
 */
public abstract class ClasspathScanningLuaResourceModule extends LuaResourceModule {

    private static final Logger LOG = LoggerFactory.getLogger(ClasspathScanningLuaResourceModule.class);

    private static final Pattern LUA_FILE_PATTERN = Pattern.compile(".*\\\\.lua", Pattern.CASE_INSENSITIVE |
                                                                                  Pattern.UNICODE_CASE);

    /**
     * Scans the given package as a java FQN name, searching for "*lua" files
     * and binding them to the resource.
     *
     * @param pkg the package, e.g. com.foo.bar
     */
    protected void scanForEdgeResources(final String pkg) {

        final Reflections  reflections = new Reflections(pkg);

        LOG.info("Scanning package \"{}\" for edge resource Lua scripts.", pkg);

        for (final String resource : reflections.getResources(LUA_FILE_PATTERN)) {
            LOG.info("Adding script \"{}\"", pkg);
            bindEdgeScriptFile(resource).onClasspath().named(resource);
        }

    }

    /**
     * Scans the given package as a java FQN name, searching for "*lua" files
     * and binding them to the resource.
     *
     * @param pkg the package, e.g. com.foo.bar
     */
    protected void scanForInternalResources(final String pkg) {

        final Reflections  reflections = new Reflections(pkg);

        LOG.info("Scanning package \"{}\" for internal resource Lua scripts.", pkg);

        for (final String resource : reflections.getResources(LUA_FILE_PATTERN)) {
            LOG.info("Adding script \"{}\"", pkg);
            bindInternalScriptFile(resource).onClasspath().named(resource);
        }

    }

}
