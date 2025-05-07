package dev.getelements.elements.service.codegen;

import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.service.codegen.CodegenService;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.openapitools.codegen.OpenAPIGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SuperUserOpenApiCodegenService implements CodegenService {

    @Override
    public File generateCore(final File spec, final String language, final String packageName, final String options) {

        final var temporaryFiles = new TemporaryFiles(SuperUserOpenApiCodegenService.class);
        final var path = temporaryFiles.createTempDirectory(packageName);
        final var args = getArgs(spec, language, options, packageName, path.toString());

        try {
            //We might need to figure out if we need a more stable way to do this.
            //Throws System.exit(1) internally if the args are malformed,
            //so try/catch sometimes doesn't do anything for us here.
            OpenAPIGenerator.main(args);
        } catch (Exception e) {
            throw new InternalException(e);
        }

        final var dir = new File(path.toString());
        final var entries = dir.list();

        if(entries == null) {
            throw new InternalException("Could not generate code because no entries were found.");
        }

        //Place this alongside the source file so that it gets cleaned up after the generated code is cleaned up
        final var zipFile = spec.getParentFile().toPath().resolve("ElementsCore.zip").toFile();

        try (final var fos = new FileOutputStream(zipFile); final var zos = new ZipOutputStream(fos)) {
            addDirToZipArchive(zos, path.toFile(), null, 0);
            return zipFile;
        } catch (Exception e) {
            throw new InternalException(e.getMessage());
        } finally {
            temporaryFiles.deleteTempFilesAndDirectories();
        }
    }

    @Override
    public File generateApplication(final File spec, final String applicationNameOrId, final String language, final String packageName, final String options) {
        //TODO: EL-101
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String[] getArgs(final File spec, final String language, final String options, final String packageName, final String path) {

        final var args = new ArrayList<String>();

        args.add("generate");
        args.add("-g");
        args.add(language);
        args.add("-i");
        args.add(spec.getAbsolutePath());
        args.add("-o");
        args.add(path);
        args.add("--package-name");
        args.add(packageName);
        args.add("--skip-validate-spec");

        if(options != null && !options.isEmpty()) {
            args.add("--additional-properties=" + options);
        }

        return args.toArray(String[]::new);
    }

    private void addDirToZipArchive(final ZipOutputStream zos,
                                    final File fileToZip,
                                    final String parentDirectoryName,
                                    final int level) throws Exception {

        if (fileToZip == null || !fileToZip.exists() || fileToZip.getName().startsWith(".")) {
            return;
        }

        final var zipEntryName = parentDirectoryName != null && !parentDirectoryName.isEmpty() && level > 1 ?
                parentDirectoryName + "/" + fileToZip.getName() :
                fileToZip.getName();

        if (fileToZip.isDirectory()) {

            for (final var file : Objects.requireNonNull(fileToZip.listFiles())) {
                addDirToZipArchive(zos, file, zipEntryName, level + 1);
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

}
