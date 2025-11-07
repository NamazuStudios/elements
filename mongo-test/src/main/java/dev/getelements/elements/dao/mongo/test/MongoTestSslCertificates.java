//package dev.getelements.elements.dao.mongo.test;
//
//import dev.getelements.elements.sdk.model.exception.InternalException;
//import dev.getelements.elements.sdk.util.TemporaryFiles;
//
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.file.Path;
//
//import static java.lang.String.format;
//
//public class MongoTestSslCertificates {
//
//    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(MongoTestSslCertificates.class);
//
//    private final Path directory = temporaryFiles.createTempDirectory() ;
//
//    private final Path caP12 = directory.resolve("ca.p12").toAbsolutePath();
//
//    private final Path clientP12 = directory.resolve("client.p12").toAbsolutePath();
//
//    public MongoTestSslCertificates() {
//        copy("ca.p12");
//        copy("ca.pem");
//        copy("ca.key");
//        copy("client.p12");
//        copy("client.pem");
//        copy("client.key");
//        copy("server.pem");
//        copy("server.key");
//    }
//
//    private void copy(final String resource) {
//
//        final var destination = directory.resolve(resource);
//
//        try (var is = MongoTestSslCertificates.class.getResourceAsStream(format("/mongo_ssl/%s", resource));
//             var os = new FileOutputStream(destination.toFile())) {
//            is.transferTo(os);
//        } catch (IOException | NullPointerException e) {
//            throw new InternalException(e);
//        }
//
//    }
//
//    public Path getDirectory() {
//        return directory;
//    }
//
//    public Path getCaP12() {
//        return caP12;
//    }
//
//    public Path getClientP12() {
//        return clientP12;
//    }
//
//}
