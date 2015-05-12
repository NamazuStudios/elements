package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.namazustudios.socialengine.client.rest.client.UserClient;
import com.namazustudios.socialengine.model.User;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by patricktwohig on 5/11/15.
 */
public class AsyncUserDataProvider extends AsyncDataProvider<User> {

    @Inject
    private UserClient userClient;

    public AsyncUserDataProvider() {
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

        final List<User> userList = new ArrayList<User>();

        for (int i = 0; i < range.getLength(); ++i) {

            final User user = new User();

            user.setName("fake_user" + (i + range.getStart()));
            user.setEmail(user.getName() + "@example.com");
            user.setActive(true);
            user.setLevel(User.Level.USER);

            userList.add(user);

        }

        updateRowCount(100, true);
        updateRowData(range.getStart(), userList);

    }

}
