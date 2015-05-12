package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.namazustudios.socialengine.client.rest.client.UserClient;
import com.namazustudios.socialengine.model.User;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by patricktwohig on 5/11/15.
 */
public class UserDataProvider extends AsyncDataProvider<User> {

    @Inject
    private UserClient userClient;

    private final List<AsyncRefreshListener> asyncRefreshListeners = new ArrayList<>();

    public UserDataProvider() {
        super(new ProvidesKey<User>() {
            @Override
            public Object getKey(User item) {
                return item.getName();
            }
        });
    }

    @Override
    protected void onRangeChanged(HasData<User> display) {

        final Range range = display.getVisibleRange();

        // TODO Wire up the actual REST calls

        new Timer() {
            @Override
            public void run() {
                populateMockData(range);
            }
        }.schedule(1000);

    }

    public void populateMockData(final Range range) {

        final List<User> userList = new ArrayList<User>();

        for (int i = 0; i < range.getLength(); ++i) {

            final User user = new User();

            user.setName("fake_user" + (i + range.getStart()));
            user.setEmail(user.getName() + "@example.com");
            user.setActive(true);
            user.setLevel(User.Level.USER);

            userList.add(user);

        }

        updateRowData(range.getStart(), userList);
        updateRowCount(100, true);
        notifyRefreshListeners();

    }

    private void notifyRefreshListeners() {

        for (final AsyncRefreshListener asyncRefreshListener : Lists.newArrayList(asyncRefreshListeners)) {
            asyncRefreshListener.onRefresh();
        }

    }

    public void addRefreshListener(final AsyncRefreshListener asyncRefreshListener) {
        asyncRefreshListeners.add(asyncRefreshListener);
    }

    public void removeRefreshListener(final  AsyncRefreshListener asyncRefreshListener) {
        asyncRefreshListeners.remove(asyncRefreshListener);
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

}
