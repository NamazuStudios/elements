package com.namazustudios.socialengine.docserve.lua;

import com.namazustudios.socialengine.docserve.StaticPathDocs;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.rt.util.TemporaryFiles;
import org.reflections.Reflections;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static java.lang.ClassLoader.getSystemClassLoader;

public class LuaStaticPathDocs implements StaticPathDocs {

    private static final String DOCS_PREFIX = "doc";

    private static final String SOURCES_PREFIX = "src";

    private static final String ELEMENTS_PREFIX = "elements";

    private static final Pattern LUA_SOURCE_FILES = Pattern.compile(".\\.lua");

    private static final TemporaryFiles files = new TemporaryFiles(LuaStaticPathDocs.class);

    private AtomicReference<Path> path = new AtomicReference<>();

    @Override
    public Path getPath() {
        final var path = this.path.get();
        if (path == null) throw new IllegalStateException();
        return path;
    }

    @Override
    public void start() {
        final var path = files.createTempFile();
        final var sources = loadElementsSources(path);
        loadLuaDocs(sources);
    }

    private Path loadElementsSources(final Path path) {

        final var sources = path
            .resolve(ELEMENTS_PREFIX)
            .resolve(SOURCES_PREFIX);

        final var classLoader = getSystemClassLoader();
        final var reflections = new Reflections("com.namazustudios", classLoader);

        reflections
            .getResources(LUA_SOURCE_FILES)
            .forEach(source -> addSource(path, source));

        return null;

    }

    private void addSource(final Path path, final String source) {

        final var dst = path.resolve(source).toFile();

        try (final var fos = new FileOutputStream(dst);
             final var bos = new BufferedOutputStream(fos);
             final var ris = getSystemClassLoader().getResourceAsStream(source)) {
            if (ris == null) throw new InternalException("Unable to read Lua source " + source);
            ris.transferTo(bos);
        } catch (IOException e) {
            throw new InternalException(e);
        }

    }

    private void loadLuaDocs(final Path path) {

    }

}
