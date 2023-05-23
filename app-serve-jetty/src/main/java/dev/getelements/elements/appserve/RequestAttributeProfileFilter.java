package dev.getelements.elements.appserve;

import dev.getelements.elements.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rt.Request;
import dev.getelements.elements.rt.Response;
import dev.getelements.elements.rt.handler.Filter;
import dev.getelements.elements.rt.handler.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.getelements.elements.model.profile.Profile.PROFILE_ATTRIBUTE;

public class RequestAttributeProfileFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestAttributeProfileFilter.class);

    private Supplier<Profile> profileSupplier;

    @Override
    public void filter(final Chain next,
                       final Session session,
                       final Request request,
                       final Consumer<Response> responseReceiver) {

        try {
            final Profile profile = getProfileSupplier().get();
            request.getAttributes().setAttribute(PROFILE_ATTRIBUTE, profile);
        } catch (UnidentifiedProfileException ex) {
            logger.trace("Skipping profile identification.");
        }

        next.next(session, request, responseReceiver);

    }

    public Supplier<Profile> getProfileSupplier() {
        return profileSupplier;
    }

    @Inject
    public void setProfileSupplier(Supplier<Profile> profileSupplier) {
        this.profileSupplier = profileSupplier;
    }

}
