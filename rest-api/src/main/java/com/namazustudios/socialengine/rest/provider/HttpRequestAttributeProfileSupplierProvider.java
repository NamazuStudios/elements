package com.namazustudios.socialengine.rest.provider;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.profile.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 6/28/17.
 */
public class HttpRequestAttributeProfileSupplierProvider implements Provider<Supplier<Profile>> {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestAttributeProfileSupplierProvider.class);

    private HttpServletRequest httpServletRequest;

    @Override
    public Supplier<Profile> get() {
        return () -> {

            final Object profile = getHttpServletRequest().getAttribute(Profile.PROFILE_ATTRIBUTE);

            if (profile == null) {
                throw new NotFoundException();
            } else if (!(profile instanceof Profile)) {
                logger.error("{} is not instance of {}", profile, Profile.class.getName());
            }

            return (Profile) profile;

        };
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    @Inject
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

}
