package dev.getelements.elements.service.codegen;

import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.service.codegen.CodegenService;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.openapitools.codegen.OpenAPIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SuperUserOpenApiCodegenService implements CodegenService {

    private static final Logger logger = LoggerFactory.getLogger(SuperUserOpenApiCodegenService.class);

    @Override
    public File generateCore(File spec, String language, String options) {
        //generate code to temp folder
        //zip code
        //clean up temp
        //return zip file

        final var tempFolderName = "codegen-" + UUID.randomUUID();
        final var temporaryFiles = new TemporaryFiles(tempFolderName);
        final var path = temporaryFiles.createTempDirectory();
        final var args = new ArrayList<String>();

        args.add("generate");
        args.add("-g");
        args.add(language);
        args.add("-i");
        args.add(spec.getAbsolutePath());
        args.add("-o");
        args.add(path.toString());
        args.add("--skip-validate-spec");

        if(options != null && !options.isEmpty()) {
            args.add("--additional-properties=" + options);
        }

        try {
            //TODO:Figure out a more stable way to do this
            //Throws System.exit(1) internally if the args are malformed, so try/catch sometimes doesn't do anything for us here
            OpenAPIGenerator.main(args.toArray(String[]::new));
        } catch (Exception e) {
            logger.error("Error generating OpenAPI", e);
        }

        final var dir = new File(path.toString());
        final var entries = dir.list();

        if(entries == null) {
            throw new InternalException("Could not generate code because no entries were found.");
        }

        try {

            //Place this alongside the source file so that it gets cleaned up later
            final var zipFile = spec.getParentFile().toPath().resolve("ElementsCore.zip").toFile();

            // Create a stream to compress data and write it to the zipfile
            final var fos = new FileOutputStream(zipFile);
            final var zos = new ZipOutputStream(fos);

            addDirToZipArchive(zos, path.toFile(), null);

            zos.flush();
            fos.flush();
            zos.close();
            fos.close();

            return zipFile;

        } catch (Exception e) {
            throw new InternalException(e.getMessage());
        } finally {
            temporaryFiles.deleteTempFilesAndDirectories();
        }
    }

    private void addDirToZipArchive(final ZipOutputStream zos,
                                    final File fileToZip,
                                    final String parentDirectoryName) throws Exception {

        if (fileToZip == null || !fileToZip.exists()) {
            return;
        }

        final var zipEntryName = parentDirectoryName != null && !parentDirectoryName.isEmpty() ?
                parentDirectoryName + "/" + fileToZip.getName() :
                fileToZip.getName();

        if (fileToZip.isDirectory()) {

            for (final var file : Objects.requireNonNull(fileToZip.listFiles())) {
                addDirToZipArchive(zos, file, zipEntryName);
            }

        } else {

            final var buffer = new byte[1024];
            final var fis = new FileInputStream(fileToZip);
            int length;

            zos.putNextEntry(new ZipEntry(zipEntryName));

            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
            fis.close();
        }
    }

    @Override
    public File generateApplication(File spec, String applicationNameOrId, String language, String options) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
