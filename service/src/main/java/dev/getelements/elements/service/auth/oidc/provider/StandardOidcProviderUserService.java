package dev.getelements.elements.service.auth.oidc.provider;

import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.auth.OidcProviderGrant;
import dev.getelements.elements.sdk.model.exception.UnauthorizedException;
import dev.getelements.elements.sdk.model.oidc.OidcProviderUserInfo;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OidcProviderUserService;
import dev.getelements.elements.sdk.service.auth.SessionTokenVerifier;
import jakarta.inject.Inject;

import java.util.Set;

import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.SCOPE_EMAIL;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.SCOPE_PROFILE;

public class StandardOidcProviderUserService implements OidcProviderUserService {

    private OidcProviderDao oidcProviderDao;

    private UserDao userDao;

    private UserUidDao userUidDao;

    private SessionTokenVerifier sessionTokenVerifier;

    @Override
    public OidcProviderUserInfo getUserInfo(final String sessionToken) {

        // The access token IS an Elements session token (minted via the shared issuer), so verification,
        // expiry, and issuer/audience checking are exactly the session-token machinery — no second verifier
        // to drift.

        final var claims = getSessionTokenVerifier().verify(sessionToken);

        final var grant = getOidcProviderDao().findGrantBySessionId(claims.getSessionId())
                .orElseThrow(() -> new UnauthorizedException("Token was not issued by this provider."));

        final var user = getUserDao().getUser(grant.getUserId());

        if (user == null) {
            throw new UnauthorizedException("Token was not issued by this provider.");
        }

        final var scopes = grant.getScope() == null
                ? Set.<String>of()
                : Set.of(grant.getScope().trim().split("\\s+"));

        final var userInfo = new OidcProviderUserInfo();
        userInfo.setSub(user.getId());

        if (scopes.contains(SCOPE_PROFILE)) {
            userInfo.setName(user.getDisplayName() != null ? user.getDisplayName() : user.getName());
            userInfo.setPreferredUsername(user.getName());
        }

        if (scopes.contains(SCOPE_EMAIL)) {
            userInfo.setEmail(user.getEmail());
            userInfo.setEmailVerified(user.getEmail() != null && findVerifiedEmailUid(user));
        }

        return userInfo;

    }

    private boolean findVerifiedEmailUid(final User user) {
        return getUserUidDao().findUserUid(user.getEmail(), UserUidDao.SCHEME_EMAIL)
                .map(uid -> uid.getVerificationStatus()
                        == dev.getelements.elements.sdk.model.user.VerificationStatus.VERIFIED)
                .orElse(false);
    }

    public OidcProviderDao getOidcProviderDao() {
        return oidcProviderDao;
    }

    @Inject
    public void setOidcProviderDao(OidcProviderDao oidcProviderDao) {
        this.oidcProviderDao = oidcProviderDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserUidDao getUserUidDao() {
        return userUidDao;
    }

    @Inject
    public void setUserUidDao(UserUidDao userUidDao) {
        this.userUidDao = userUidDao;
    }

    public SessionTokenVerifier getSessionTokenVerifier() {
        return sessionTokenVerifier;
    }

    @Inject
    public void setSessionTokenVerifier(SessionTokenVerifier sessionTokenVerifier) {
        this.sessionTokenVerifier = sessionTokenVerifier;
    }

}
