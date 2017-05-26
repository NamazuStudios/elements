package com.namazustudios.socialengine.rt.lua.guice;

import com.google.common.base.Function;
import com.namazustudios.socialengine.rt.Path;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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
     * This uses the default naming strategy whereby the package name is trimmed from
     * the path and he lua extension is dropped.  For example, if "foo/bar.lua"
     * exists on the classpath, the script will serve requests out of "/bar".
     *
     * This also means that no two handler resource scripts be named the same.
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
     * pattern, and binding them to the resource.
     *
     * @param pkg the package, e.g. com.foo.bar
     * @param filePattern the file pattern eg ".*\.lua"
     * @param bootstrapPathGenerator a {@link Function} used to determine the bootstrap path from the name
     */
    protected void scanForEdgeResources(final String pkg,
                                        final Pattern filePattern,
                                        final Function<String, Path> bootstrapPathGenerator) {

        final Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackages(pkg)
                .filterInputsBy(new FilterBuilder().includePackage(pkg))
                .setScanners(new ResourcesScanner()));

        LOG.info("Scanning package \"{}\" for handler resource Lua scripts.", pkg);

        for (final String resource : reflections.getResources(filePattern)) {
            final Path bootstrapBath = bootstrapPathGenerator.apply(resource);

            LOG.info("Adding handler resource script \"{}\" from package {} at bootstrap path {} ",
                    resource, pkg, bootstrapBath.toNormalizedPathString());

            bindEdgeScriptFile(resource)
                .toBootstrapPath(bootstrapBath)
                .fromClasspath()
                .named(resource);
        }

    }

    /**
     * Scans the given package as a java FQN name, searching for "*lua" files
     * and binding them to the resource.
     *
     * The script is {@link javax.inject.Named} for the script file.
     *
     * @param pkg the package, e.g. com.foo.bar
     */
    protected void scanForInternalResources(final String pkg) {
        scanForInternalResources(pkg, LUA_FILE_PATTERN, new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable final String input) {
                return input.substring(pkg.length()).replaceFirst("/", "");
            }
        });
    }

    /**
     * Scans the given package as a java FQN name, searching for "*lua" files
     * and binding them to the resource.
     *
     * @param pkg the package, e.g. com.foo.bar
     */
    protected void scanForInternalResources(final String pkg,
                                            final Pattern filePattern,
                                            final Function<String, String> nameGenerator) {

        final Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(pkg)
                        .filterInputsBy(new FilterBuilder().includePackage(pkg))
                        .setScanners(new ResourcesScanner()));

        LOG.info("Scanning package \"{}\" for worker resource Lua scripts.", pkg);

        for (final String resource : reflections.getResources(filePattern)) {
            final String name = nameGenerator.apply(resource);
            LOG.info("Adding worker resource script \"{}\" from package {} named {} ", resource, pkg, name);
            bindInternalScriptFile(resource).fromClasspath().named(name);
        }

    }

}
