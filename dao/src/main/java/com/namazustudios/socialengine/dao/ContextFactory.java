package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.rt.Context;

public interface ContextFactory {

    Context getContextForApplication(final String applicationId);

}
