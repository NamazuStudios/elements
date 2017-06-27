package com.namazustudios.socialengine.model.profile;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;

/**
 *
 *
 * Created by patricktwohig on 6/27/17.
 */
public class Profile {

    private User user;

    private Application application;

    private String imageUrl;

    private String displayName;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
