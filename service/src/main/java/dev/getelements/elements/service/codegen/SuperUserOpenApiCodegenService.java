package dev.getelements.elements.service.codegen;

import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.service.codegen.CodegenService;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.openapitools.codegen.OpenAPIGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SuperUserOpenApiCodegenService implements CodegenService {

    @Override
    public File generateCore(String language, String options) {
        //generate code to temp folder
        //zip code
        //clean up temp
        //return zip file

        final var tempFolderName = "codegen-" + UUID.randomUUID();
        final var temporaryFiles = new TemporaryFiles(tempFolderName);
        final var path = temporaryFiles.createTempDirectory();
        final var args = new ArrayList<String>();
        final var zipFile = path.resolve("ElementsCore.zip").toFile();

        args.add("-o");
        args.add(path.toString());
        args.add("-g");
        args.add(language);
        args.add("--skip-validate-spec");

        if(options != null && !options.isEmpty()) {
            args.add("--additional-properties=" + options);
        }

        OpenAPIGenerator.main(args.toArray(String[]::new));

        final var dir = new File(path.toString());
        final var entries = dir.list();

        if(entries == null) {
            throw new InternalException("Could not generate code because no entries were found.");
        }

        try {
            // Create a stream to compress data and write it to the zipfile
            final var out = new ZipOutputStream(new FileOutputStream(zipFile));
            final var buffer = new byte[4096]; // Create a buffer for copying
            int bytes_read;

            // Loop through all entries in the directory
            for (final var entry : entries) {

                final var file = new File(dir, entry);

                if (file.isDirectory()) {
                    continue; // Don't zip sub-directories
                }

                final var in = new FileInputStream(file); // Stream to read file
                final var zipEntry = new ZipEntry(file.getPath()); // Make a ZipEntry

                out.putNextEntry(zipEntry); // Store entry

                while ((bytes_read = in.read(buffer)) != -1) {
                    // Copy bytes
                    out.write(buffer, 0, bytes_read);
                }

                in.close(); // Close input stream
            }

            // When we're done with the whole loop, close the output stream
            out.close();

            return zipFile;

        } catch (Exception e) {
            throw new InternalException(e.getMessage());
        }
    }

    @Override
    public File generateApplication(String applicationNameOrId, String language, String options) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
