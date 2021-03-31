package com.namazustudios.socialengine.service.guice;

import com.namazustudios.socialengine.*;
import com.namazustudios.socialengine.service.FirebaseSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FirebaseSessionServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseSessionServiceTest.class);

    private final FirebaseTestClient ftc = new FirebaseTestClient();

    private FirebaseEmailPasswordSignUpResponse signupResult;

    private FirebaseUsernamePasswordSignInResponse signinResult;

    private FirebaseSessionService firebaseSessionService;

    @BeforeClass
    public void signupUser() {

        final var signup = ftc.randomUserSignup();
        signupResult = ftc.signUp(signup);
        logger.info("Successfully created user.");

        final var signin = new FirebaseUsernamePasswordSignInRequest(signup);
        signinResult = ftc.signIn(signin);
        logger.info("Successfully logged-in user.");

    }

    @Test
    public void testVerify() {

    }

    @AfterClass
    public void destroyAccount() {
        final var request = new FirebaseDeleteAccountRequest();
        request.setIdToken(signinResult.getIdToken());
        ftc.deleteAccount(request);
        logger.info("Successfully deleted account.");
    }

}
