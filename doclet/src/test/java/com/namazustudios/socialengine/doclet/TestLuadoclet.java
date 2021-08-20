package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.doclet.lua.LDocStubDoclet;
import com.namazustudios.socialengine.rt.util.TemporaryFiles;
import org.testng.annotations.Test;

import javax.tools.ToolProvider;

import java.nio.file.Path;

import static org.testng.Assert.assertEquals;

public class TestLuadoclet {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(TestLuadoclet.class);

    private final Path testDirectory = temporaryFiles.createTempDirectory();

    @Test
    public void testGenerateFirst() {

        final var result = ToolProvider.getSystemDocumentationTool().run(
            System.in, System.out, System.err,
            "-sourcepath", "doclet/src/test/java",
            "-subpackages", "com.namazustudios",
            "-d", testDirectory.toString(),
            "-doclet", LDocStubDoclet.class.getName()
        );

        assertEquals(0, result);

    }

}
