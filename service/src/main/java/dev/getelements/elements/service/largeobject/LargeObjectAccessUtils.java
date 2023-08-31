package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.SubjectRequest;
import dev.getelements.elements.model.largeobject.Subjects;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.UserService;
import dev.getelements.elements.service.profile.UserProfileService;

import javax.inject.Inject;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

public class LargeObjectAccessUtils {

    private String cdnUrl;

    private UserService userService;

    private UserProfileService userProfileService;

    Subjects fromRequest(final SubjectRequest subjectRequest) {
        Subjects subjects = new Subjects();
        subjects.setWildcard(subjectRequest.isWildcard());
        subjects.setUsers(subjectRequest.getUserIds().stream().map(userService::getUser).collect(toList()));
        subjects.setProfiles(subjectRequest.getProfileIds().stream().map(userProfileService::getProfile).collect(toList()));
        return subjects;
    }

    String assignAutomaticPath(final String mimeType) {
        return format("/_auto/%s/%s.bin", mimeType, randomUUID());
    }

    boolean hasUserAccess(Subjects subjects, User user) {
        return subjects.isWildcard() ||
                subjects.getUsers().contains(user) ||
                subjects.getProfiles().contains(userProfileService.getCurrentProfile());
    }

    boolean hasWriteAccess(AccessPermissions accessPermissions, User user) {
        return hasUserAccess(accessPermissions.getWrite(), user);
    }

    boolean hasReadAccess(AccessPermissions accessPermissions, User user) {
        return hasUserAccess(accessPermissions.getRead(), user);
    }

    boolean hasDeleteAccess(AccessPermissions accessPermissions, User user) {
        return hasUserAccess(accessPermissions.getDelete(), user);
    }


    LargeObject setCdnUrlToObject(final LargeObject largeObject) {
        final var url = format("%s/%s", cdnUrl, largeObject.getId());
        largeObject.setUrl(url);
        return largeObject;
    }

    @Inject
//    public void setCdnUrl(@Named(CDN_OUTSIDE_URL) String cdnUrl) {
//        this.cdnUrl = cdnUrl;
//    }
    public void setCdnUrl(String cdnUrl) {
        this.cdnUrl = cdnUrl;
    }

    @Inject
    void setUserProfileService(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Inject
    void setUserService(UserService userService) {
        this.userService = userService;
    }

}
