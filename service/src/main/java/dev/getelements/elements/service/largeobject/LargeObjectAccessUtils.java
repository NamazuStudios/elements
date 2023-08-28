package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.SubjectRequest;
import dev.getelements.elements.model.largeobject.Subjects;

import javax.security.auth.Subject;

import static dev.getelements.elements.model.largeobject.Subjects.anonymousSubject;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

class LargeObjectAccessUtils {

    AccessPermissions createAnonymousAccess() {
        Subjects anonymousSubject = anonymousSubject();
        AccessPermissions access = new AccessPermissions();
        access.setRead(anonymousSubject);
        access.setWrite(anonymousSubject);
        return access;
    }

    Subjects fromRequest(final SubjectRequest subjectRequest) {
        // TODO: Properly convert from subject. Look up each subject (eg User and Profile) in the database.
        return new Subjects();
    }

    String assignAutomaticPath(final String mimeType) {
        return format("/_auto/%s/%s.bin", mimeType, randomUUID());
    }

}
