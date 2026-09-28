package dev.getelements.elements.service;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.service.auth.totp.AnonTotpEnrollmentService;
import dev.getelements.elements.service.auth.totp.TotpEnrollmentServiceProvider;
import dev.getelements.elements.service.auth.totp.UserTotpEnrollmentService;
import jakarta.inject.Provider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.model.user.User.Level.UNPRIVILEGED;
import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static org.testng.Assert.assertSame;

/**
 * TOTP enrollment is a personal account security feature, not an admin-only one -- covers that regular USER
 * accounts (not just SUPERUSER) are routed to the real self-service implementation, and only truly
 * unauthenticated (UNPRIVILEGED/no-level) callers are forbidden.
 */
public class TotpEnrollmentServiceProviderTest {

    private TotpEnrollmentServiceProvider provider;

    private UserTotpEnrollmentService userTotpEnrollmentService;

    private AnonTotpEnrollmentService anonTotpEnrollmentService;

    @BeforeMethod
    public void setup() {

        userTotpEnrollmentService = new UserTotpEnrollmentService();
        anonTotpEnrollmentService = new AnonTotpEnrollmentService();

        provider = new TotpEnrollmentServiceProvider();
        provider.setUserTotpEnrollmentService((Provider<UserTotpEnrollmentService>) () -> userTotpEnrollmentService);
        provider.setAnonTotpEnrollmentService((Provider<AnonTotpEnrollmentService>) () -> anonTotpEnrollmentService);

    }

    private static User userWithLevel(final User.Level level) {
        final var user = new User();
        user.setLevel(level);
        return user;
    }

    @Test
    public void testUserLevelCanSelfEnroll() {
        provider.setUser(userWithLevel(USER));
        assertSame(provider.get(), userTotpEnrollmentService);
    }

    @Test
    public void testSuperuserLevelCanSelfEnroll() {
        provider.setUser(userWithLevel(SUPERUSER));
        assertSame(provider.get(), userTotpEnrollmentService);
    }

    @Test
    public void testUnprivilegedIsForbidden() {
        provider.setUser(userWithLevel(UNPRIVILEGED));
        assertSame(provider.get(), anonTotpEnrollmentService);
    }

    @Test
    public void testNullLevelIsForbidden() {
        provider.setUser(new User());
        assertSame(provider.get(), anonTotpEnrollmentService);
    }

}
