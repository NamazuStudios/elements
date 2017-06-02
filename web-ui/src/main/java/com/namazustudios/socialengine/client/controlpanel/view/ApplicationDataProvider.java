package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.namazustudios.socialengine.client.rest.client.ApplicationClient;
import com.namazustudios.socialengine.model.application.Application;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/1/17.
 */
public class ApplicationDataProvider extends AbstractSearchableDataProvider<Application> {

    @Inject
    private ApplicationClient applicationClient;

    public ApplicationDataProvider() {
        super(new ProvidesKey<Application>() {
            @Override
            public Object getKey(Application item) {
                return item.getId();
            }
        });
    }

    @Override
    protected void onRangeChanged(HasData<Application> display) {

    }

}
