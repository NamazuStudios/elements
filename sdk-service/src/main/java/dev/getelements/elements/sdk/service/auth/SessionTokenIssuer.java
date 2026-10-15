package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;

/**
 * Issues session credentials. All login paths route through a single shared issuer, which creates the
 * server-side session record and returns a {@link SessionCreation} whose session secret is a signed
 * session token carrying the session's identity. The server-side record is retained as the source of
 * truth for revocation and session management.
 */
@ElementServiceExport
public interface SessionTokenIssuer {

    /**
     * Creates the server-side record for the supplied {@link Session} and returns a
     * {@link SessionCreation} whose session secret is a signed session token.
     *
     * @param session the session to issue
     * @return the {@link SessionCreation}, bearing a signed session token
     */
    SessionCreation issue(Session session);

}
