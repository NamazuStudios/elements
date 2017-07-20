package com.namazustudios.socialengine.service.match;

import com.namazustudios.socialengine.service.MatchService;

import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.unimplemented;

/**
 * Provider for the {@link MatchService}.
 *
 * Created by patricktwohig on 7/19/17.
 */
public class MatchServiceProvider implements Provider<MatchService> {

    @Override
    public MatchService get() {
        return unimplemented(MatchService.class);
    }

}
