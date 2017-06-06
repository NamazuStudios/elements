package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.namazustudios.socialengine.client.rest.client.ApplicationProfileClient;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;
import java.util.Collections;

/**
 * Created by patricktwohig on 6/5/17.
 */
public class ApplicationProfileDataProvider extends AbstractSearchableDataProvider<ApplicationProfile> {

    @Inject
    private ApplicationProfileClient applicationProfileClient;

    private Application parentApplication;

    @Override
    protected void onRangeChanged(final HasData<ApplicationProfile> display) {
        if (getParentApplication() == null) {
            display.setRowData(0, Collections.emptyList());
        } else {

            final Range range = display.getVisibleRange();

            applicationProfileClient.getAllProfiles(
                    getParentApplication().getId(),
                    range.getStart(),
                    range.getLength(),
                    getSearchFilter(),
                    new MethodCallback<Pagination<ApplicationProfile>>() {

                @Override
                public void onFailure(Method method, Throwable exception) {
                    notifyErrorListeners(exception);
                }

                @Override
                public void onSuccess(Method method, Pagination<ApplicationProfile> applicationProfilePagination) {

                    if (!applicationProfilePagination.getObjects().isEmpty()) {
                        updateRowData(applicationProfilePagination.getOffset(), applicationProfilePagination.getObjects());
                    }

                    updateRowCount(applicationProfilePagination.getTotal(), !applicationProfilePagination.isApproximation());
                    notifyRefreshListeners();

                }

            });
        }
    }

    public Application getParentApplication() {
        return parentApplication;
    }

    public void setParentApplication(Application parentApplication) {
        this.parentApplication = parentApplication;
    }

}
