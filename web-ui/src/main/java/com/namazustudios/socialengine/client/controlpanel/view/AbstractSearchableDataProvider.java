package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.namazustudios.socialengine.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patricktwohig on 6/25/15.
 */
public abstract class AbstractSearchableDataProvider<ModelT> extends AsyncDataProvider<ModelT> {

    private final List<AsyncRefreshListener> asyncRefreshListeners = new ArrayList<>();

    private final List<ErrorListener> errorListeners = new ArrayList<>();

    private String searchFilter = "";

    public AbstractSearchableDataProvider() {}

    public AbstractSearchableDataProvider(ProvidesKey<ModelT> keyProvider) {
        super(keyProvider);
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(final String search) {
        this.searchFilter = Strings.nullToEmpty(search).trim();
    }

    public void addRefreshListener(final AsyncRefreshListener asyncRefreshListener) {
        asyncRefreshListeners.add(asyncRefreshListener);
    }

    public void removeRefreshListener(final  AsyncRefreshListener asyncRefreshListener) {
        asyncRefreshListeners.remove(asyncRefreshListener);
    }

    public void addErrorListener(final ErrorListener errorListener) {
        errorListeners.add(errorListener);
    }

    public void removeErrorListener(final  ErrorListener errorListener) {
        errorListeners.remove(errorListener);
    }

    protected void notifyRefreshListeners() {

        for (final AsyncRefreshListener asyncRefreshListener : Lists.newArrayList(asyncRefreshListeners)) {
            asyncRefreshListener.onRefresh();
        }

    }

    protected void notifyErrorListeners(final Throwable throwable) {

        for (final ErrorListener errorListener : Lists.newArrayList(errorListeners)) {
            errorListener.onError(throwable);
        }

    }

    /**
     * Used to signal async changes to the user data provider.
     */
    public interface AsyncRefreshListener {

        /**
         * Called any time the data is refreshed, loaded, or changed.
         */
        void onRefresh();

    }

    /**
     * Used to signal an error.
     */
    public interface ErrorListener {

        void onError(final Throwable throwable);

    }

}
