package com.namazustudios.socialengine.docserve.lua;

import com.namazustudios.socialengine.docserve.StaticPathDocs;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.rt.util.ProcessLogger;
import com.namazustudios.socialengine.rt.util.TemporaryFiles;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.nio.file.Files.createDirectories;

public class LuaStaticPathDocs implements StaticPathDocs {

    private static final Logger logger = LoggerFactory.getLogger(LuaStaticPathDocs.class);

    private static final String DOCS_PREFIX = "doc";

    private static final String SOURCES_PREFIX = "src";

    private static final String ELEMENTS_PREFIX = "elements";

    private static final List<Pattern> EXCLUSIONS = List.of(
        Pattern.compile("main.lua"),
        Pattern.compile("example.*")
    );

    private static final Pattern LUA_SOURCE_FILES = Pattern.compile(".*\\.lua");

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

        final var path = files.createTempDirectory();
        final var sources = loadElementsSources(path);
        loadLuaDocs(sources);

        if (!this.path.compareAndSet(null, path)) {
            throw new IllegalStateException("Already started.");
        }

    }

    private Path loadElementsSources(final Path path) {

        final var sources = path
            .resolve(ELEMENTS_PREFIX)
            .resolve(SOURCES_PREFIX);

        final var reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forJavaClassPath())
            .setScanners(new ResourcesScanner()));

        reflections
            .getResources(LUA_SOURCE_FILES)
            .forEach(source -> addSource(sources, source));

        return sources;

    }

    private void addSource(final Path path, final String source) {

        final var excluded = EXCLUSIONS
            .stream()
            .map(pat -> pat.matcher(source).matches())
            .filter(match -> match)
            .findFirst()
            .orElse(false);

        if (excluded) {
            logger.debug("Excluding: {} from {}", source, path);
            return;
        }

        final var dst = path.resolve(source);
        logger.debug("Including {} -> {}", source, dst);

        try {
            createDirectories(dst.getParent());
        } catch (IOException e) {
            throw new InternalException(e);
        }

        try (final var fos = new FileOutputStream(dst.toFile());
             final var bos = new BufferedOutputStream(fos);
             final var ris = getSystemClassLoader().getResourceAsStream(source)) {

            if (ris == null) throw new InternalException("Unable to read Lua source " + source);

            try (final var bis = new BufferedInputStream(ris)) {
                bis.transferTo(bos);
            }

        } catch (IOException e) {
            throw new InternalException(e);
        }

    }

    private void loadLuaDocs(final Path sources) {

        final var paths = sources.getParent().resolve(DOCS_PREFIX);

        try {

            final var process = new ProcessBuilder()
                .command("ldoc", "-d", paths.toString(), sources.toString())
                .start();

            final var processLogger = new ProcessLogger("ldoc", process, logger);
            processLogger.start();

            process.waitFor();

        } catch (IOException | InterruptedException e) {
            throw new InternalException(e);
        }

    }

}
