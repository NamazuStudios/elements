package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.namazustudios.socialengine.client.rest.client.ApplicationClient;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/1/17.
 */
public class ApplicationDataProvider extends AbstractSearchableDataProvider<Application> {

    @Inject
    private ApplicationClient applicationClient;

    public ApplicationDataProvider() {
        super(item -> item.getId());
    }

    @Override
    protected void onRangeChanged(HasData<Application> display) {
        final Range range = display.getVisibleRange();

        applicationClient.getApplications(
                range.getStart(),
                range.getLength(),
                getSearchFilter(),
                new MethodCallback<Pagination<Application>>() {
                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        notifyErrorListeners(exception);
                    }

                    @Override
                    public void onSuccess(Method method, Pagination<Application> applicationPagination) {

                        if (applicationPagination.getTotal() == 0) {
                            updateRowCount(0, true);
                        } else {
                            updateRowData(range.getStart(), applicationPagination.getObjects());
                        }

                        notifyRefreshListeners();

                    }

                });
    }

}
