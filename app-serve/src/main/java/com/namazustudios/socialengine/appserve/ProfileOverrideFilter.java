package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.rt.handler.Session;
import com.namazustudios.socialengine.security.SessionSecretHeader;

import javax.inject.Inject;
import java.util.function.Consumer;

public class ProfileOverrideFilter implements Filter {

    private ProfileDao profileDao;

    @Override
    public void filter(final Chain next, Session session,
                       final Request request,
                       final Consumer<Response> responseReceiver) {

        final RequestHeader header = request.getHeader();
        final SessionSecretHeader sessionSecretHeader = new SessionSecretHeader(header::getHeader);
        final String overrideProfileId = sessionSecretHeader.getOverrideProfileId();

        if (overrideProfileId == null) {
            next.next(session, request, responseReceiver);
        } else {
            final Profile override = getProfileDao().findActiveProfile(overrideProfileId);
            request.getAttributes().setAttribute(Profile.PROFILE_ATTRIBUTE, override);
            next.next(session, request, responseReceiver);
        }

    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

}
