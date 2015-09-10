package com.namazustudios.socialengine.rt.lua.guice;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.Ref;
import java.util.Set;
import java.util.regex.Matcher;
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

    private static final Pattern LUA_FILE_PATTERN = Pattern.compile(".*\\.lua", Pattern.CASE_INSENSITIVE |
                                                                                Pattern.UNICODE_CASE);
    /**
     * Scans the given package as a java FQN name, searching for "*lua" files
     * and binding them to the resource.
     *
     * @param pkg the package, e.g. com.foo.bar
     */
    protected void scanForEdgeResources(final String pkg) {
        scanForEdgeResources(pkg, LUA_FILE_PATTERN, new Function<String, Path>() {
            @Nullable
            @Override
            public Path apply(final String resource) {
                final String scriptFile = resource.substring(pkg.length());
                final String scriptFilePath = scriptFile.substring(0, scriptFile.length() - 4);
                return new Path(scriptFilePath);
            }
        });
    }
    /**
     * Scans the given package as a java FQN name, searching for lua files matching the given
     * patterrn, and binding them to the resource.
     *
     * @param pkg the package, e.g. com.foo.bar
     * @param filePattern the file pattern eg ".*\.lua"
     * @param bootstrapPathGenerator a {@link Function} used to determine the boostrap path from the name
     */
    protected void scanForEdgeResources(final String pkg,
                                        final Pattern filePattern,
                                        final Function<String, Path> bootstrapPathGenerator) {

        final Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackages(pkg)
                .filterInputsBy(new FilterBuilder().includePackage(pkg))
                .setScanners(new ResourcesScanner()));

        LOG.info("Scanning package \"{}\" for edge resource Lua scripts.", pkg);

        for (final String resource : reflections.getResources(filePattern)) {
            LOG.info("Adding edge resource script \"{}\" from package {}", resource, pkg);
            final Path bootstrapBath = bootstrapPathGenerator.apply(resource);
            bindEdgeScriptFile(resource).onBootstrapPath(bootstrapBath).onClasspath();
        }

    }

    /**
     * Scans the given package as a java FQN name, searching for "*lua" files
     * and binding them to the resource.
     *
     * @param pkg the package, e.g. com.foo.bar
     */
    protected void scanForInternalResources(final String pkg) {

        final Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(pkg)
                        .filterInputsBy(new FilterBuilder().includePackage(pkg))
                        .setScanners(new ResourcesScanner()));

        LOG.info("Scanning package \"{}\" for internal resource Lua scripts.", pkg);

        for (final String resource : reflections.getResources(LUA_FILE_PATTERN)) {
            LOG.info("Adding internal resource script \"{}\" from package {}", resource, pkg);
            bindInternalScriptFile(resource).onClasspath().named(resource);
        }

    }

}
