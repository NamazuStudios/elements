package com.namazustudios.socialengine;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class FirebaseTestClient {

    private static final String API_KEY = "AIzaSyDRPtlf937sneL3M89NBD5V-4aqkfqOZYY";

    private final Client client = ClientBuilder.newClient();

    public FirebaseEmailPasswordSignUpRequest randomUserSignup() {

        final var req = new FirebaseEmailPasswordSignUpRequest();
        final var random = ThreadLocalRandom.current();

        final var username = format("tester.%s@namazustudios.com", randomUUID());

        final var password = new StringBuilder();
        for (int i = 0; i < 4; ++i) password.append((char)random.nextInt('a', 'z'));
        for (int i = 0; i < 4; ++i) password.append((char)random.nextInt('A', 'Z'));
        for (int i = 0; i < 4; ++i) password.append((char)random.nextInt('0', '9'));

        req.setEmail(username);
        req.setPassword(password.toString());

        return req;

    }

    public FirebaseEmailPasswordSignUpResponse signUp(final FirebaseEmailPasswordSignUpRequest request) {
        return client
            .target("https://identitytoolkit.googleapis.com/v1/accounts:signUp")
            .queryParam("key", API_KEY)
            .request()
            .post(entity(request, APPLICATION_JSON_TYPE), FirebaseEmailPasswordSignUpResponse.class);
    }

    public FirebaseUsernamePasswordSignInResponse signIn(final FirebaseUsernamePasswordSignInRequest request) {
        return client
            .target("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword")
            .queryParam("key", API_KEY)
            .request()
            .post(entity(request, APPLICATION_JSON_TYPE), FirebaseUsernamePasswordSignInResponse.class);
    }

    public void deleteAccount(final FirebaseDeleteAccountRequest request) {
        client
            .target("https://identitytoolkit.googleapis.com/v1/accounts:delete")
            .queryParam("key", API_KEY)
            .request()
            .post(entity(request, APPLICATION_JSON_TYPE), Void.class);
    }

}

