package com.namazustudios.socialengine.client.controlpanel.view.shortlink;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.namazustudios.socialengine.client.controlpanel.view.application.AbstractSearchableDataProvider;
import com.namazustudios.socialengine.client.rest.client.internal.ShortLinkClient;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ShortLink;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/25/15.
 */
public class ShortLinkDataProvider extends AbstractSearchableDataProvider<ShortLink> {

    @Inject
    private ShortLinkClient shortLinkClient;

    public ShortLinkDataProvider() {
        super(item -> item.getId());
    }

    @Override
    protected void onRangeChanged(HasData<ShortLink> display) {

        final Range visibleRange = display.getVisibleRange();

        shortLinkClient.getShortLinks(
                visibleRange.getStart(),
                visibleRange.getLength(),
                new MethodCallback<Pagination<ShortLink>>() {

                    @Override
                    public void onFailure(Method method, Throwable throwable) {
                        notifyErrorListeners(throwable);
                    }

                    @Override
                    public void onSuccess(Method method, Pagination<ShortLink> pagination) {
                        notifyRefreshListeners();

                        if (!pagination.getObjects().isEmpty()) {
                            updateRowData(visibleRange.getStart(), pagination.getObjects());
                        }

                        updateRowCount(pagination.getTotal(), !pagination.isApproximation());
                        notifyRefreshListeners();

                    }

                });

    }

}

