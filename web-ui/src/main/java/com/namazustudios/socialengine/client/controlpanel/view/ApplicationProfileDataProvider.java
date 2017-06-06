package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.view.client.HasData;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationProfile;

/**
 * Created by patricktwohig on 6/5/17.
 */
public class ApplicationProfileDataProvider extends AbstractSearchableDataProvider<ApplicationProfile> {

    private Application parentApplication;

    @Override
    protected void onRangeChanged(final HasData<ApplicationProfile> display) {
        // TODO Fetch Profiles
    }

}
