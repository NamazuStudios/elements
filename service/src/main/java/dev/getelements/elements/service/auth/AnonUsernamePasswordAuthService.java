package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.TotpLoginChallengeDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.auth.CaptchaVerifyRequest;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.auth.MfaChallengeRequiredException;
import dev.getelements.elements.sdk.model.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.sdk.model.session.MfaVerifyRequest;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.auth.CaptchaService;
import dev.getelements.elements.sdk.service.auth.TotpVerificationService;
import dev.getelements.elements.sdk.service.auth.UsernamePasswordAuthService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by patricktwohig on 4/1/15.
 */
@Singleton
public class AnonUsernamePasswordAuthService implements UsernamePasswordAuthService {

    private static final long MFA_CHALLENGE_TIMEOUT_MINUTES = 5;

    private UserDao userDao;

    private SessionDao sessionDao;

    private ProfileDao profileDao;

    private ApplicationDao applicationDao;

    private ValidationHelper validationHelper;

    private CaptchaService captchaService;

    private TotpVerificationService totpVerificationService;

    private TotpLoginChallengeDao totpLoginChallengeDao;

    private long sessionTimeoutSeconds;

    @Override
    public SessionCreation createSession(final UsernamePasswordSessionRequest usernamePasswordSessionRequest) {

        getValidationHelper().validateModel(usernamePasswordSessionRequest);

        final var userId = usernamePasswordSessionRequest.getUserId().trim();
        final var password = usernamePasswordSessionRequest.getPassword();
        final var profileId = usernamePasswordSessionRequest.getProfileId();
        final var profileSelector = usernamePasswordSessionRequest.getProfileSelector();

        final var applicationId = usernamePasswordSessionRequest.getApplicationNameOrId();

        final var user = getUserDao().validateUserPassword(userId, password);

        // The admin panel is the only client of this shared endpoint that authenticates SUPERUSER accounts, so
        // scoping the CAPTCHA gate to SUPERUSER logins enforces the admin login form's requirement without
        // affecting regular USER-level game client logins.
        if (User.Level.SUPERUSER.equals(user.getLevel())) {
            requireValidCaptchaIfEnabled(usernamePasswordSessionRequest.getCaptchaToken());
        }

        if (getTotpVerificationService().isRequiredFor(user)) {

            final var expiry = new Timestamp(currentTimeMillis() + MILLISECONDS.convert(MFA_CHALLENGE_TIMEOUT_MINUTES, MINUTES));

            final var challengeId = getTotpLoginChallengeDao().createChallenge(
                    user.getId(),
                    profileId,
                    profileSelector,
                    applicationId,
                    expiry
            );

            throw new MfaChallengeRequiredException(challengeId, expiry.getTime());

        }

        return buildSession(user, userId, profileId, profileSelector, applicationId);

    }

    @Override
    public SessionCreation completeMfaChallenge(final MfaVerifyRequest mfaVerifyRequest) {

        getValidationHelper().validateModel(mfaVerifyRequest);

        final var challenge = getTotpLoginChallengeDao()
                .find(mfaVerifyRequest.getChallengeId())
                .orElseThrow(() -> new NotFoundException("MFA challenge not found or expired."));

        final var user = getUserDao().getUser(challenge.getUserId());

        // Only consume the challenge on success -- a wrong guess must not burn it, or a legitimate
        // follow-up attempt (e.g. falling back to a recovery code after mistyping a TOTP code) would
        // incorrectly see "challenge not found" instead of getting to actually retry.
        if (!getTotpVerificationService().verify(user, mfaVerifyRequest.getCode())) {
            throw new ForbiddenException("Invalid authentication code.");
        }

        getTotpLoginChallengeDao().consume(challenge.getId());

        return buildSession(
                user,
                challenge.getUserId(),
                challenge.getProfileId(),
                challenge.getProfileSelector(),
                challenge.getApplicationNameOrId()
        );

    }

    private SessionCreation buildSession(final User user,
                                          final String userId,
                                          final String profileId,
                                          final String profileSelector,
                                          final String applicationId) {

        final var profile = getProfileIfSpecified(profileId)
                .or(() -> selectProfileIfSpecified(user, profileSelector))
                .or(() -> selectPrimaryProfileIfApplicationSpecified(user, applicationId));

        profile.ifPresent(p -> {
            if (!Objects.equals(user, p.getUser())) {
                throw new ForbiddenException("Invalid credentials for " + userId);
            }
        });

        final var session = new Session();

        session.setUser(user);
        session.setProfile(profile.orElse(null));
        profile.map(Profile::getApplication).ifPresent(session::setApplication);

        final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();
        session.setExpiry(expiry);

        return getSessionDao().create(session);

    }

    private void requireValidCaptchaIfEnabled(final String captchaToken) {

        if (!getCaptchaService().getPublicConfiguration().isEnabled()) {
            return;
        }

        if (captchaToken == null || captchaToken.isBlank()) {
            throw new ForbiddenException("CAPTCHA verification is required.");
        }

        final var request = new CaptchaVerifyRequest();
        request.setToken(captchaToken);

        if (!getCaptchaService().verify(request).isSuccess()) {
            throw new ForbiddenException("CAPTCHA verification failed.");
        }

    }

    private Optional<Profile> getProfileIfSpecified(final String profileId) {
        return profileId == null ?
                Optional.empty() :
                Optional.of(getProfileDao().getActiveProfile(profileId));
    }

    private Optional<Profile> selectProfileIfSpecified(final User user, final String selector) {

        if (selector == null) {
            return Optional.empty();
        }

        final var query = String.format(".ref.user:%s AND %s", user.getId(), selector);

        final var profiles = getProfileDao()
                .getActiveProfiles(0, 1, query)
                .getObjects();

        if (profiles.isEmpty()) {
            throw new ProfileNotFoundException("Profile not found.");
        }

        return Optional.of(profiles.get(0));

    }

    private Optional<Profile> selectPrimaryProfileIfApplicationSpecified(final User user,
                                                                          final String applicationNameOrId) {

        if (applicationNameOrId == null) {
            return Optional.empty();
        }

        return getApplicationDao()
                .findApplication(applicationNameOrId)
                .flatMap(application -> getProfileDao().findPrimaryProfile(user.getId(), application.getId()));

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Inject
    public void setSessionTimeoutSeconds(@Named(SESSION_TIMEOUT_SECONDS) long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public CaptchaService getCaptchaService() {
        return captchaService;
    }

    @Inject
    public void setCaptchaService(@Named(UNSCOPED) CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    public TotpVerificationService getTotpVerificationService() {
        return totpVerificationService;
    }

    @Inject
    public void setTotpVerificationService(@Named(UNSCOPED) TotpVerificationService totpVerificationService) {
        this.totpVerificationService = totpVerificationService;
    }

    public TotpLoginChallengeDao getTotpLoginChallengeDao() {
        return totpLoginChallengeDao;
    }

    @Inject
    public void setTotpLoginChallengeDao(TotpLoginChallengeDao totpLoginChallengeDao) {
        this.totpLoginChallengeDao = totpLoginChallengeDao;
    }

}
