package dev.getelements.elements.cdnserve;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.cluster.path.Path;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.nio.file.Paths;

import static dev.getelements.elements.sdk.service.Constants.CDN_FILE_DIRECTORY;
import static dev.getelements.elements.sdk.service.Constants.CDN_SERVE_ENDPOINT;
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static java.util.stream.Collectors.joining;

public class StaticContentServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(StaticContentServlet.class);

    private String serveEndpoint;

    private String contentDirectory;

    private ApplicationDao applicationDao;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        final var pathInfo = req.getPathInfo();

        if (req.getPathInfo() == null) {
            resp.setStatus(SC_NOT_FOUND);
            logger.debug("No file specified.");
            return;
        }

        final Path staticContentPath;

        try {
            staticContentPath = new Path(pathInfo);
        } catch (IllegalArgumentException ex) {
            resp.setStatus(SC_NOT_FOUND);
            logger.debug("Invalid path: {}", pathInfo);
            return;
        }

        if (staticContentPath.getComponents().size() < 2) {
            resp.setStatus(SC_NOT_FOUND);
            logger.debug("Did not specify file: {}", pathInfo);
            return;
        }

        final var applicationNameOrId = staticContentPath.getComponent(0);
        final var applicationOptional = getApplicationDao().findActiveApplication(applicationNameOrId);

        if (applicationOptional.isEmpty()) {
            resp.setStatus(SC_NOT_FOUND);
            logger.debug("No application: {}", applicationNameOrId);
            return;
        }

        final var applicationBase = Paths.get(
                getContentDirectory(),
                applicationOptional.get().getName()
        ).toRealPath();

        final var components = staticContentPath
                .getComponents();

        final var relativePathString = components
                .subList(1, components.size())
                .stream()
                .collect(joining(applicationBase.getFileSystem().getSeparator()));

        final var applicationFilePath = Paths.get(applicationBase.toString() + File.separatorChar + relativePathString).toRealPath();

        if (applicationFilePath.startsWith(applicationBase)) {
            try (var fis = new FileInputStream(applicationFilePath.toFile());
                 var bis = new BufferedInputStream(fis)) {
                bis.transferTo(resp.getOutputStream());
            } catch (FileNotFoundException ex) {
                resp.setStatus(SC_NOT_FOUND);
                logger.debug("File not found {}", applicationFilePath, ex);
            } catch (IOException ex) {
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
                logger.error("Caught IO Exception reading file {}", applicationFilePath, ex);
            }

        } else {
            logger.debug("Detected attempt to access path outside of hierarchy: {}", applicationFilePath);
            resp.setStatus(SC_NOT_FOUND);
        }

    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    private String getContentDirectory() {
        return contentDirectory;
    }

    @Inject
    private void setContentDirectory(@Named(CDN_FILE_DIRECTORY) String contentDirectory) {
        this.contentDirectory = contentDirectory;
    }

    private String getServeEndpoint() {
        return serveEndpoint;
    }

    @Inject
    private void setServeEndpoint(@Named(CDN_SERVE_ENDPOINT) String serveEndpoint) {
        this.serveEndpoint = serveEndpoint;
    }

}
