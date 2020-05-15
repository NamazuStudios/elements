package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.exception.profile.UnidentifiedProfileException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.rt.handler.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.model.profile.Profile.PROFILE_ATTRIBUTE;

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
