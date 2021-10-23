package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.doclet.lua.LDocStubDoclet;
import com.namazustudios.socialengine.rt.util.TemporaryFiles;
import org.testng.annotations.Test;

import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.nio.file.Files.exists;
import static java.util.stream.Collectors.joining;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class TestLuadoclet {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(TestLuadoclet.class);

    private final Path testDirectory = temporaryFiles.createTempDirectory();

    @Test
    public void testGenerateFirst() throws IOException {

        var path = Paths.get("doclet/src/test/java");
        if (!exists(path)) path = Paths.get("src/test/java");

        final var result = ToolProvider.getSystemDocumentationTool().run(
            System.in, System.out, System.err,
            "-sourcepath", path.toAbsolutePath().toString(),
            "-subpackages", "com.namazustudios",
            "-d", testDirectory.toString(),
            "-doclet", LDocStubDoclet.class.getName()
        );

        assertEquals(0, result);

    }

}
