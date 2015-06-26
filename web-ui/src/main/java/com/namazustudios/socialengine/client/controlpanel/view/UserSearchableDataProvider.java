package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.common.base.Strings;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.namazustudios.socialengine.client.rest.client.UserClient;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/11/15.
 */
public class UserSearchableDataProvider extends AbstractSearchableDataProvider<User> {

    @Inject
    private UserClient userClient;

    public UserSearchableDataProvider() {
        super(new ProvidesKey<User>() {
            @Override
            public Object getKey(User item) {
                return item.getName();
            }
        });
    }

    @Override
    protected void onRangeChanged(final HasData<User> display) {

        final Range range = display.getVisibleRange();

        userClient.getUsers(range.getStart(), range.getLength(), getSearchFilter(),
            new MethodCallback<Pagination<User>>() {

                @Override
                public void onFailure(Method method, Throwable throwable) {
                    notifyErrorListeners(throwable);
                }

                @Override
                public void onSuccess(Method method, Pagination<User> userPagination) {

                    if (!userPagination.getObjects().isEmpty()) {
                        updateRowData(range.getStart(), userPagination.getObjects());
                    }

                    updateRowCount(userPagination.getTotal(), !userPagination.isApproximation());
                    notifyRefreshListeners();

                }

            });

    }

}
