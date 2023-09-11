package dev.getelements.elements.cdnserve;

import dev.getelements.elements.exception.BaseException;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.LargeObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

public class LargeObjectServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LargeObjectServlet.class);

    private Provider<LargeObjectService> largeObjectServiceProvider;

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {
        
        final String largeObjectId = req.getPathInfo().substring(1);
        final LargeObjectService service = getLargeObjectServiceProvider().get();

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

}
