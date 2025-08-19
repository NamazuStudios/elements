package dev.getelements.elements.service.codegen;

import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.service.codegen.CodegenService;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SuperUserOpenApiCodegenService implements CodegenService {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(SuperUserOpenApiCodegenService.class);

    @Override
    public File generateCore(final File spec, final String language, final String packageName, final String options) {

        final var path = temporaryFiles.createTempDirectory(packageName);

        try {
            final var generator = new DefaultGenerator();
            final var configurator = new CodegenConfigurator();
            final var additionalOptions = options.split(",");

            configurator.setInputSpec(spec.getAbsolutePath());
            configurator.setOutputDir(path.toString());
            configurator.setGeneratorName(language);
            configurator.setPackageName(packageName);
            configurator.setValidateSpec(false);

            //Parse out the args, with a check to make sure an empty option value wasn't passed through
            for (final var option : additionalOptions) {

                final var kvp = option.split("=");

                if(kvp.length == 2) {
                    configurator.addAdditionalProperty(kvp[0], kvp[1]);
                }
            }

            generator.opts(configurator.toClientOptInput());
            generator.generate();

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
