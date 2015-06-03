package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.namazustudios.socialengine.client.rest.client.UserClient;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by patricktwohig on 5/11/15.
 */
public class UserDataProvider extends AsyncDataProvider<User> {

    @Inject
    private UserClient userClient;

    private final List<AsyncRefreshListener> asyncRefreshListeners = new ArrayList<>();

    private final List<ErrorListener> errorListeners = new ArrayList<>();

    private String search;

    public UserDataProvider() {
        super(new ProvidesKey<User>() {
            @Override
            public Object getKey(User item) {
                return item.getName();
            }
        });
    }

    public void filterWithSearch(final String search) {
        this.search = Strings.nullToEmpty(search).trim();
    }

    @Override
    protected void onRangeChanged(final HasData<User> display) {

        final Range range = display.getVisibleRange();

        userClient.getUsers(range.getStart(), range.getLength(), Strings.nullToEmpty(search),
            new MethodCallback<Pagination<User>>() {

                @Override
                public void onFailure(Method method, Throwable throwable) {
                    notifyErrorListeners(throwable);
                }

                @Override
                public void onSuccess(Method method, Pagination<User> userPagination) {
                    updateRowData(range.getStart(), userPagination.getObjects());
                    updateRowCount(userPagination.getTotal(), !userPagination.isApproximation());
                    notifyRefreshListeners();
                }

            });

    }

    private void notifyRefreshListeners() {

        for (final AsyncRefreshListener asyncRefreshListener : Lists.newArrayList(asyncRefreshListeners)) {
            asyncRefreshListener.onRefresh();
        }

    }

    private void notifyErrorListeners(final Throwable throwable) {

        for (final ErrorListener errorListener : Lists.newArrayList(errorListeners)) {
            errorListener.onError(throwable);
        }

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
