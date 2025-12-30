package dev.getelements.elements.service.defaults;

import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.dao.OAuth2AuthSchemeDao;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.model.auth.OAuth2RequestKeyValue;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ElementServiceExport(DefaultOAuth2SchemeConfiguration.class)
public class DefaultOAuth2SchemeConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOAuth2SchemeConfiguration.class);

    private OAuth2AuthSchemeDao oAuth2AuthSchemeDao;

    @ElementEventConsumer(ElementLoader.SYSTEM_EVENT_ELEMENT_LOADED)
    public void init() {
        createSteamConfiguration();
    }

    private void createSteamConfiguration() {

        final var authScheme = new OAuth2AuthScheme();
        final var authSchemeName = "Steam";
        final var scheme = getAuthSchemeDao().findAuthScheme(authSchemeName);

        if(scheme.isPresent()) {
            return;
        }

        authScheme.setName(authSchemeName);
        authScheme.setValidationUrl("https://api.steampowered.com/ISteamUserAuth/AuthenticateUserTicket/v1/");
        authScheme.setHeaders(List.of(
                new OAuth2RequestKeyValue("x-webapi-key", "Copy from Steam publishing settings.", false),
                new OAuth2RequestKeyValue("Content-Type", "application/x-www-form-urlencoded", false)
        ));
        authScheme.setParams(List.of(
                new OAuth2RequestKeyValue("appid", "Steam AppId", false),
                new OAuth2RequestKeyValue("ticket", "Ticket from GetAuthSessionTicket (Sent from frontend)", true),
                new OAuth2RequestKeyValue("identity", "You define this when requesting the ticket. Must match frontend.", false)
        ));
        authScheme.setResponseIdMapping("steamid");

        try {
            getAuthSchemeDao().createAuthScheme(authScheme);
        } catch (Exception e) {
            logger.debug("Steam OAuth2 Auth Scheme detected, skipping default...");
        }

    }

    public OAuth2AuthSchemeDao getAuthSchemeDao() {
        return oAuth2AuthSchemeDao;
    }

    @Inject
    public void setAuthSchemeDao(OAuth2AuthSchemeDao oAuth2AuthSchemeDao) {
        this.oAuth2AuthSchemeDao = oAuth2AuthSchemeDao;
    }
}
