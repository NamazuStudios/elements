package dev.getelements.elements.service.defaults;

import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.dao.OidcAuthSchemeDao;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ElementServiceExport(DefaultOidcSchemeConfiguration.class)
public class DefaultOidcSchemeConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOidcSchemeConfiguration.class);

    private OidcAuthSchemeDao authSchemeDao;

    @ElementEventConsumer(ElementLoader.SYSTEM_EVENT_ELEMENT_LOADED)
    public void init() {
        createGoogleScheme();
        createAppleScheme();
        createFacebookScheme();
        createSteamScheme();
        createPlaystationScheme();
        createXBoxScheme();
        createSwitchScheme();
    }

    private void createGoogleScheme() {

        final var name = "Google";
        final var issuer = "https://accounts.google.com";
        final var schemeOptional = getAuthSchemeService().findAuthScheme(issuer);

        if(schemeOptional.isPresent()) {

            final var authScheme = schemeOptional.get();

            if(authScheme.getName() == null) {
                authScheme.setName(name);
                tryUpdateOidcScheme(authScheme);
            }

            return;
        }

        final var request = new OidcAuthScheme();
        final var keys = List.of(
                new JWK("RS256", "d9740a70b0972dccf75fa88bc529bd16a30573bd", "RSA", "sig", "AQAB", "oeS547_9wjr2KSN8kA8shy-1arjHHxrx8QeARyWQ9tjQZ8xuF62y-2Ffz0J9F8A_vjrtWCv-ApD1m2v86qs6ZhCXYjvFOPzu7eehcSIojxqgjcN8rqMmhOloPVll_xsc1XXs3djFYL4cGaozJ4b7C5HWQqCJwkKqDTUPAfNTgQG-CSFlGVMM9Yu5ZElsiQIvP_DHfmyMsSIfmi5xxJD_xIBxumh9C8pOOcarw2oi8eLqtyj9jnnjEJncm51PsjkyATCzcMKSFIGFr-UPVnH4-4mYpeqwwYzcvb95DH-exQANjYLANFiSbyRU0SxzJ39yKPAPIBwqrA37BVwsD5AJvw"),
                new JWK("RS256", "3628258601113e6576a45337365fe8b8973d1671", "RSA", "sig", "AQAB", "vHJNSdOKUAG53oCGHbEp2PJFX-NksFDrw1_TEzK8yF72Jbp8cYebwkoZpCkr2THVAmRuvDe8GuuXYyRih9w7APwAH0aNy8og4Q1rqPuX-q1TAqO9KXYJNd2VIaICwY2IvY3IgQNu0r9GKouSBeeaXGBlUYi2IR74T4ICOwcpJYTQOE2GWcWeri7iaeFzMfqKa0NJrv6f7paGA0DNu0PggNpgOQMbZoriWc7-PGa7lP4QrStpGikgNOcbGfEw53LeB6dbw72uCCpGbd1iuhzv6M6B-7gLQEp4188mAgjSkmr4TruyZ36Nn4gK_FTOFI44QNMvAGUBJ1L7M49V0KyELQ")
        );

        request.setName(name);
        request.setKeys(keys);
        request.setIssuer(issuer);
        request.setKeysUrl("https://www.googleapis.com/oauth2/v3/certs");
        request.setMediaType("application/json");

        tryCreateOidcScheme(request);
    }

    private void createAppleScheme() {

        final var name = "Apple";
        final var issuer = "https://appleid.apple.com";
        final var schemeOptional = getAuthSchemeService().findAuthScheme(issuer);

        if(schemeOptional.isPresent()) {

            final var authScheme = schemeOptional.get();

            if(authScheme.getName() == null) {
                authScheme.setName(name);
                tryUpdateOidcScheme(authScheme);
            }

            return;
        }

        final var request = new OidcAuthScheme();
        final var keys = List.of(
                new JWK("RS256", "FftONTxoEg", "RSA", "sig", "AQAB", "wio-SFzFvKKQ9vl5ctaYSi09o8k3Uh7r6Ht2eJv-hSaZ6A6xTXVIBVSm0KvPxaJlpjYPTCcl2sdEyXlD2Uh1khUKU7r9ON3rpN8pFHAere5ig_JGVEShxmt5E_jzMymYnSfkoSW44ulevQeUwP_MiC5VC1KJjTfD73ghX0tQ0-_RjTJJ2cLyFC4VFNboBMCVioUrz8IA3c0KIOl507qswQvMsh2vBTMDDSJfippAGLzUiWXxUlid-vyOC8GCtag61taSorxCw14irk-tsh7hWjDDkSTFn2gChPMfXXj10_lCv0UG29TVUVCAsay4pszzgmc4zwhgSsqQRd939BJexw"),
                new JWK("RS256", "pggnQeNCOU", "RSA", "sig", "AQAB", "xyWY7ydqTVHRzft5fPZmTuD9Ahk7-_2_IekZGy07Ovhj5IhYyVU8Hq5j0_c9m9tSdJTRdKmNjMURpY4ZJ_9rd3EOQ_WnYHM2cZIQ5y3f_WxeElnv_f2fKDruA-ERaQ6duov-3NAXC3oTWdXuRGRLbbfOVCahTjvnAA8YBRUe3llW7ZvTG14g-fAEQVlMYDxxCsbjtBJiUzKxbH-8KvhIhP9AJtiLDfiK1yzVJ7Qn6HNm5AUsFQKOAgTqxDMJkhi7pyntTyxhpkLYTEndaPRXth_LM3hVmaoFb3P3TsPCbDjSEbKy1wAndfPSzUk6qjyyBYhdXH0sgVpKMBAdggylLQ"),
                new JWK("RS256", "dMlERBaFdK", "RSA", "sig", "AQAB", "ryLWkB74N6SJNRVBjKF6xKMfP-QW3AAsJotv0LjVtf7m4NZNg_gTL78e7O8wmvngF8FuzBrvqf1mGW17Ct8BgNK6YXxnoGL0YLmlwXbmCZvTXki0VlEW1PDXeViWy7qXaCp2caF5v4OOdPsgroxNO_DgJRTuA_izJ4DFZYHCHXwojfdWJiDYG67j5PlD5pXKGx7zaqyryjovZTEII_Z1_bhFCRUZRjfJ3TVoK0fZj2z7iAZWjn33i-V3zExUhwzEyeuGph0118NfmOLCUEy_Jd4xvLf_X4laPpe9nq8UeORfs72yz2qH7cHDKL85W6oG08Gu05JWuAs5Ay49WxJrmw"),
                new JWK("RS256", "rBRfVmqsjn", "RSA", "sig", "AQAB", "pPOaiF5yL-y42FaKg9PYASR5-rdTK7NEiteNUAzNp0zkta-HW-tgLNLNlsft3zcrsgOLqXxhX7qzlI3JGH-wSs7_v2XNSg57QhOTxPDqtUfy5DegtiSOgwE947OBTwCWo2R6cGZD1T8ysfO2HuKheq2hEwZU4Y-8qT19WWOhZHs4CVt7A5mzpIgWuUVw766VTyqrqKev32DOUPIqFocFz3tuty95S9t_OYnaPCcET-b6DV_eT7psPhqhl5nNUm0lzkCQ53-9kxQNJxBciy0wiBcAexD4KppKRRD3evFpOSxD1R6Kg2DIG5UnbVVqn5nhZA9RH-t50f_biqV3KlSHJQ"),
                new JWK("RS256", "T8tIJ1zSrO", "RSA", "sig", "AQAB", "teUbLrwScsjVrcFAvSrfben3eQaEca3ESBegGh_wdGuLKw6QgwDxY3fC1_WeSVnkJXx72ddw3j2inoADnTyzuNa_PwDSmvJhOhmzOmoltmtKHteGdaXrqMohO6A85WxVKbN7pzDqwZJNrdY12LOltlI8PHIG-elAbKM2XOHiJaZnLpAVckKy6MQYsEExpPB3plGxWZElqwNZY6SUDVeN-o9qg5FJOFg7T7iTVVEagws4DM6uZNMDQGtqg9V9VqPQkUzC-sYd5eqbB9LqH4iN5F6OB7BmD3g3jCu9zgh3O9V24N43EruBCNrmP0xLP5ZliKqozoAcd1nv71HuVm6mgQ")
        );

        request.setName(name);
        request.setKeys(keys);
        request.setIssuer(issuer);
        request.setKeysUrl("https://appleid.apple.com/auth/keys");
        request.setMediaType("application/json");

        tryCreateOidcScheme(request);
    }

    private void createFacebookScheme() {

    }

    private void createSteamScheme() {

    }

    private void createPlaystationScheme() {

    }

    private void createXBoxScheme() {

    }

    private void createSwitchScheme() {

    }

    private void tryUpdateOidcScheme(final OidcAuthScheme scheme) {
        try {
            getAuthSchemeService().updateAuthScheme(scheme);
        } catch (Exception e) {
            logger.debug(scheme.getName() + " OIDC Auth Scheme unable to update, skipping default...");
        }
    }

    private void tryCreateOidcScheme(final OidcAuthScheme scheme) {
        try {
            getAuthSchemeService().createAuthScheme(scheme);
        } catch (Exception e) {
            logger.debug(scheme.getName() + " OIDC Auth Scheme detected, skipping default...");
        }
    }

    @Inject
    public void setAuthSchemeService(OidcAuthSchemeDao authSchemeDao) {
        this.authSchemeDao = authSchemeDao;
    }

    public OidcAuthSchemeDao getAuthSchemeService() {
        return authSchemeDao;
    }

}
