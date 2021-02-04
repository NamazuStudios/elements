package com.namazustudios.socialengine.setup;

import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.keyboard.DefaultKeyboardInteractiveAuthenticator;
import org.apache.sshd.server.auth.keyboard.InteractiveChallenge;
import org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

import java.util.List;

public class Deny implements PasswordAuthenticator, KeyboardInteractiveAuthenticator {

    private final DefaultKeyboardInteractiveAuthenticator delegate = new DefaultKeyboardInteractiveAuthenticator();

    @Override
    public InteractiveChallenge generateChallenge(
            final ServerSession session,
            final String username,
            final String lang,
            final String subMethods) throws Exception {
        return delegate.generateChallenge(session, username, lang, subMethods);
    }

    @Override
    public boolean authenticate(
            final ServerSession session,
            final String username,
            final List<String> responses) throws Exception {
        return false;
    }

    @Override
    public boolean authenticate(
            final String username,
            final String password,
            final ServerSession session) throws PasswordChangeRequiredException, AsyncAuthException {
        return false;
    }

}
