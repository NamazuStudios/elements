package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.service.GameOnSessionService;

import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.unimplemented;

public class GameOnSessionServiceProvider implements Provider<GameOnSessionService> {

    @Override
    public GameOnSessionService get() {
        return unimplemented(GameOnSessionService.class);
    }

}
