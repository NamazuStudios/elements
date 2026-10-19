package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.dao.SuperUserInviteTokenDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.invite.CreateSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.RedeemSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.SuperUserInvite;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import dev.getelements.elements.rest.test.model.SuperUserInvitePagination;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * End-to-end coverage of the superuser email-invite REST surface (#91): issuance and listing are
 * superuser-only, redemption requires the authenticated account to hold a verified email UID
 * matching the invited address and elevates the account, revocation kills pending invites, and
 * tokens are single-use.
 */
public class SuperUserInviteApiTest {

    private static final String INVITED_EMAIL_PREFIX = "superuser.invite.e2e";
    private static final String INVITED_EMAIL_DOMAIN = "@example.com";

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(SuperUserInviteApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private UserDao userDao;

    @Inject
    private UserUidDao userUidDao;

    @Inject
    private SuperUserInviteTokenDao superUserInviteTokenDao;

    @Inject
    private ClientContext superUserContext;

    @Inject
    private ClientContext regularUserContext;

    @Inject
    private ClientContext inviteeContext;

    private String invitedEmail;

    private User invitee;

    @BeforeClass
    private void setUp() {

        superUserContext.createSuperuser("superuserInviteApiTestIssuer").createSession();
        regularUserContext.createUser("superuserInviteApiTestBystander").createSession();

        inviteeContext.createUser("superuserInviteApiTestInvitee");
        invitee = inviteeContext.getUser();

        // The invite is bound to the invitee's email; the account must control it via a verified
        // email-scheme UID for redemption to be permitted.
        invitedEmail = format("%s.%s%s", INVITED_EMAIL_PREFIX, invitee.getId(), INVITED_EMAIL_DOMAIN);

        final var uid = new UserUid();
        uid.setId(invitedEmail);
        uid.setScheme(UserUidDao.SCHEME_EMAIL);
        uid.setUserId(invitee.getId());
        uid.setVerificationStatus(VerificationStatus.VERIFIED);
        userUidDao.createUserUid(uid);

        // createUserUid persists only the scheme binding; the verification status must be
        // transitioned explicitly (this is the same path the email-verification flow uses).
        userUidDao.updateVerificationStatus(invitedEmail, UserUidDao.SCHEME_EMAIL, VerificationStatus.VERIFIED);

        inviteeContext.createSession();
    }

    @Test
    public void issue_asRegularUser_denied() {

        final var request = new CreateSuperUserInviteRequest();
        request.setEmail(invitedEmail);

        final var response = client
                .target(apiRoot + "/invite/superuser")
                .request()
                .header("Authorization", format("Bearer %s", regularUserContext.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);
    }

    @Test
    public void issue_unauthenticated_denied() {

        final var request = new CreateSuperUserInviteRequest();
        request.setEmail(invitedEmail);

        // No credentials: the filter chain resolves an unprivileged user, and the service layer
        // rejects elevation management for anything below SUPERUSER.
        final var response = client
                .target(apiRoot + "/invite/superuser")
                .request()
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);
    }

    @Test(dependsOnMethods = {"issue_asRegularUser_denied", "issue_unauthenticated_denied"})
    public void issue_asSuperuser_createsPendingInviteAndSendsNoEmailWithoutBaseUrl() {

        final var request = new CreateSuperUserInviteRequest();
        request.setEmail(invitedEmail);

        final var response = client
                .target(apiRoot + "/invite/superuser")
                .request()
                .header("Authorization", format("Bearer %s", superUserContext.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var invite = response.readEntity(SuperUserInvite.class);

        assertNotNull(invite.getToken());
        assertEquals(invite.getInvitedEmail(), invitedEmail);
        assertEquals(invite.getStatus(), SuperUserInvite.Status.PENDING);
        assertNotNull(invite.getExpiry());
        assertNotNull(invite.getCreatedAt());

        // Round-trip: the stored record reflects the same state.
        final var stored = superUserInviteTokenDao.findTokenAnyState(invite.getToken()).orElseThrow();
        assertEquals(stored.getInvitedBy().getId(), superUserContext.getUser().getId());
    }

    @Test(dependsOnMethods = "issue_asSuperuser_createsPendingInviteAndSendsNoEmailWithoutBaseUrl")
    public void issue_duplicatePending_denied() {

        final var request = new CreateSuperUserInviteRequest();
        request.setEmail(invitedEmail);

        final var response = client
                .target(apiRoot + "/invite/superuser")
                .request()
                .header("Authorization", format("Bearer %s", superUserContext.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);
    }

    @Test(dependsOnMethods = "issue_asSuperuser_createsPendingInviteAndSendsNoEmailWithoutBaseUrl")
    public void list_asSuperuser_containsIssuedInvite() {

        final var response = client
                .target(apiRoot + "/invite/superuser")
                .request()
                .header("Authorization", format("Bearer %s", superUserContext.getSessionSecret()))
                .get();

        assertEquals(response.getStatus(), 200);

        final var pagination = response.readEntity(SuperUserInvitePagination.class);

        assertNotNull(pagination);
        assertTrue(pagination.getObjects().stream()
                .anyMatch(i -> invitedEmail.equals(i.getInvitedEmail())),
                "Issued invite must appear in the listing.");
    }

    @Test(dependsOnMethods = "issue_asSuperuser_createsPendingInviteAndSendsNoEmailWithoutBaseUrl")
    public void list_asRegularUser_denied() {

        final var response = client
                .target(apiRoot + "/invite/superuser")
                .request()
                .header("Authorization", format("Bearer %s", regularUserContext.getSessionSecret()))
                .get();

        assertEquals(response.getStatus(), 403);
    }

    @Test(dependsOnMethods = "issue_asSuperuser_createsPendingInviteAndSendsNoEmailWithoutBaseUrl")
    public void redeem_byNonMatchingAccount_denied() {

        // The bystander has no verified email UID matching the invited address.
        final var request = new RedeemSuperUserInviteRequest();
        request.setToken(superUserInviteTokenDao.findPendingInviteByEmail(invitedEmail).orElseThrow().getToken());

        final var response = client
                .target(apiRoot + "/invite/superuser/redeem")
                .request()
                .header("Authorization", format("Bearer %s", regularUserContext.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        // The bystander must remain a plain user.
        final var bystander = userDao.getUser(regularUserContext.getUser().getId());
        assertTrue(bystander.getLevel() != SUPERUSER, "A non-matching account must never be elevated.");
    }

    @Test(dependsOnMethods = {"redeem_byNonMatchingAccount_denied"})
    public void redeem_byMatchingAccount_elevatesToSuperuser() {

        final var token = superUserInviteTokenDao.findPendingInviteByEmail(invitedEmail).orElseThrow().getToken();

        final var request = new RedeemSuperUserInviteRequest();
        request.setToken(token);

        final var response = client
                .target(apiRoot + "/invite/superuser/redeem")
                .request()
                .header("Authorization", format("Bearer %s", inviteeContext.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var invite = response.readEntity(SuperUserInvite.class);
        assertEquals(invite.getStatus(), SuperUserInvite.Status.REDEEMED);

        final var elevated = userDao.getUser(invitee.getId());
        assertEquals(elevated.getLevel(), SUPERUSER, "Redemption must elevate the invited account.");
    }

    @Test(dependsOnMethods = "redeem_byMatchingAccount_elevatesToSuperuser")
    public void redeem_secondTime_notFound() {

        final var request = new RedeemSuperUserInviteRequest();
        request.setToken(superUserInviteTokenDao.findPendingInviteByEmail(invitedEmail)
                .map(SuperUserInvite::getToken)
                .orElseGet(() -> superUserInviteTokenDao.getInvites(0, 100).getObjects().stream()
                        .filter(i -> invitedEmail.equals(i.getInvitedEmail()))
                        .map(SuperUserInvite::getToken)
                        .findFirst()
                        .orElseThrow()));

        final var response = client
                .target(apiRoot + "/invite/superuser/redeem")
                .request()
                .header("Authorization", format("Bearer %s", inviteeContext.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 404, "Redeemed invites are single-use.");
    }

    @Test(dependsOnMethods = "redeem_secondTime_notFound")
    public void revoke_afterRedeemed_denied() {

        final var token = superUserInviteTokenDao.getInvites(0, 100).getObjects().stream()
                .filter(i -> invitedEmail.equals(i.getInvitedEmail()))
                .map(SuperUserInvite::getToken)
                .findFirst()
                .orElseThrow();

        final var response = client
                .target(apiRoot + "/invite/superuser/" + token)
                .request()
                .header("Authorization", format("Bearer %s", superUserContext.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 400, "Only pending invites can be revoked.");
    }

    @Test
    public void revoke_asRegularUser_denied() {

        final var token = superUserInviteTokenDao.createInvite(
                superUserContext.getUser(),
                format("%s.revoke.%s%s", INVITED_EMAIL_PREFIX, System.nanoTime(), INVITED_EMAIL_DOMAIN),
                new java.sql.Timestamp(System.currentTimeMillis() + 3600_000),
                new java.sql.Timestamp(System.currentTimeMillis()));

        final var response = client
                .target(apiRoot + "/invite/superuser/" + token)
                .request()
                .header("Authorization", format("Bearer %s", regularUserContext.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 403);

        // The invite must remain pending.
        assertEquals(superUserInviteTokenDao.findToken(token).orElseThrow().getStatus(),
                SuperUserInvite.Status.PENDING);
    }

    @Test(dependsOnMethods = "revoke_asRegularUser_denied")
    public void revoke_pendingInvite_succeedsAndKillsRedemption() {

        final var email = format("%s.revokeflow.%s%s", INVITED_EMAIL_PREFIX, System.nanoTime(), INVITED_EMAIL_DOMAIN);

        final var token = superUserInviteTokenDao.createInvite(
                superUserContext.getUser(),
                email,
                new java.sql.Timestamp(System.currentTimeMillis() + 3600_000),
                new java.sql.Timestamp(System.currentTimeMillis()));

        final var revokeResponse = client
                .target(apiRoot + "/invite/superuser/" + token)
                .request()
                .header("Authorization", format("Bearer %s", superUserContext.getSessionSecret()))
                .delete();

        assertEquals(revokeResponse.getStatus(), 200);

        final var revoked = revokeResponse.readEntity(SuperUserInvite.class);
        assertEquals(revoked.getStatus(), SuperUserInvite.Status.REVOKED);

        // The revoked token no longer redeems.
        final var redeemRequest = new RedeemSuperUserInviteRequest();
        redeemRequest.setToken(token);

        final var redeemResponse = client
                .target(apiRoot + "/invite/superuser/redeem")
                .request()
                .header("Authorization", format("Bearer %s", inviteeContext.getSessionSecret()))
                .post(Entity.entity(redeemRequest, APPLICATION_JSON));

        assertEquals(redeemResponse.getStatus(), 404, "Revoked invites must not be redeemable.");
    }

}
