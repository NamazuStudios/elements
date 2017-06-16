package com.namazustudios.socialengine.client.controlpanel.view.application;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.namazustudios.socialengine.client.rest.client.ApplicationConfigurationClient;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/5/17.
 */
public class ApplicationConfigurationDataProvider extends AbstractSearchableDataProvider<ApplicationConfiguration> {

    @Inject
    private ApplicationConfigurationClient applicationConfigurationClient;

    private Application parentApplication;

    @Override
    protected void onRangeChanged(final HasData<ApplicationConfiguration> display) {
        if (getParentApplication() == null) {
            updateRowCount(0, true);
            notifyRefreshListeners();
        } else {

            final Range range = display.getVisibleRange();

            applicationConfigurationClient.getAllProfiles(
                    getParentApplication().getId(),
                    range.getStart(),
                    range.getLength(),
                    getSearchFilter(),
                    new MethodCallback<Pagination<ApplicationConfiguration>>() {

                @Override
                public void onFailure(Method method, Throwable exception) {
                    notifyErrorListeners(exception);
                }

                @Override
                public void onSuccess(Method method, Pagination<ApplicationConfiguration> applicationProfilePagination) {

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
