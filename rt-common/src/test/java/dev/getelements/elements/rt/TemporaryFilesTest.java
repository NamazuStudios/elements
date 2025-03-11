package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.testng.annotations.Test;

public class TemporaryFilesTest {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(TemporaryFiles.class);

    @Test(invocationCount = 500, threadPoolSize = 100)
    public void writeFiles() {
        temporaryFiles.createTempFile();
    }

    @Test(invocationCount = 500, threadPoolSize = 100)
    public void writeDirectory() {
        temporaryFiles.createTempDirectory();
    }

}
