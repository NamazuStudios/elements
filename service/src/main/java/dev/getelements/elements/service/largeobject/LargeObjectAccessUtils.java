package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.Subjects;

import static dev.getelements.elements.model.largeobject.Subjects.anonymousSubject;

class LargeObjectAccessUtils {

    AccessPermissions createAnonymousAccess() {
        Subjects anonymousSubject = anonymousSubject();
        AccessPermissions access = new AccessPermissions();
        access.setRead(anonymousSubject);
        access.setWrite(anonymousSubject);
        return access;
    }

}
