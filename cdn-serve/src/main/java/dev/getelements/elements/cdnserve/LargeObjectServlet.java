package dev.getelements.elements.cdnserve;

import dev.getelements.elements.sdk.model.exception.BaseException;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.service.largeobject.LargeObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static dev.getelements.elements.sdk.model.Constants.CDN_PUBLIC_MAX_AGE;
import static jakarta.servlet.http.HttpServletResponse.*;

public class LargeObjectServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LargeObjectServlet.class);

    private Provider<LargeObjectService> largeObjectServiceProvider;

    private int publicMaxAgeSeconds;

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {
        final String pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            resp.setStatus(SC_NOT_FOUND);
            return;
        }

        final String largeObjectId = pathInfo.substring(1);
        final LargeObjectService service = getLargeObjectServiceProvider().get();
        final LargeObject largeObject = service.findLargeObject(largeObjectId)
                .orElseThrow(NotFoundException::new);
        final boolean isDownload = "true".equals(req.getParameter("download"));

        final boolean publiclyReadable = largeObject.getAccessPermissions() != null
                && largeObject.getAccessPermissions().getRead() != null
                && largeObject.getAccessPermissions().getRead().isWildcard();

        final Date lastModified = largeObject.getLastModified();
        final String etag = "\"" + largeObjectId + "-"
                + (lastModified == null ? "0" : Long.toHexString(lastModified.getTime())) + "\"";
        final String cacheControl = publiclyReadable
                ? "public, max-age=" + publicMaxAgeSeconds + ", must-revalidate"
                : "private, no-store";

        final String ifNoneMatch = req.getHeader("If-None-Match");
        if (etag.equals(ifNoneMatch)) {
            resp.setHeader("ETag", etag);
            resp.setHeader("Cache-Control", cacheControl);
            resp.setStatus(SC_NOT_MODIFIED);
            return;
        }

        resp.setHeader("ETag", etag);
        if (lastModified != null) {
            resp.setDateHeader("Last-Modified", lastModified.getTime());
        }
        resp.setHeader("Cache-Control", cacheControl);
        resp.setHeader("Vary", "Cookie, Authorization");

        if (isDownload) {
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + largeObject.getOriginalFilename() + "\"");
        } else {
            resp.setHeader("Content-Disposition", "inline; filename=\"" + largeObject.getOriginalFilename() + "\"");
        }

        resp.setContentType(largeObject.getMimeType());

        try (var input = service.readLargeObjectContent(largeObjectId)) {
            input.transferTo(resp.getOutputStream());
            resp.setStatus(SC_OK);
        } catch (ForbiddenException ex) {
            resp.setStatus(SC_FORBIDDEN);
        } catch (NotFoundException ex) {
            resp.setStatus(SC_NOT_FOUND);
        } catch (BaseException ex) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            logger.error("Caught error processing large object", ex);
        }

    }

    public Provider<LargeObjectService> getLargeObjectServiceProvider() {
        return largeObjectServiceProvider;
    }

    @Inject
    public void setLargeObjectServiceProvider(Provider<LargeObjectService> largeObjectServiceProvider) {
        this.largeObjectServiceProvider = largeObjectServiceProvider;
    }

    @Inject
    public void setPublicMaxAgeSeconds(@Named(CDN_PUBLIC_MAX_AGE) int publicMaxAgeSeconds) {
        this.publicMaxAgeSeconds = publicMaxAgeSeconds;
    }

}
