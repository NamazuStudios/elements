package com.namazustudios.socialengine.client.controlpanel.view.application;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.rest.client.FacebookApplicationConfigurationClient;
import com.namazustudios.socialengine.client.rest.client.GooglePlayApplicationConfigurationClient;
import com.namazustudios.socialengine.client.rest.client.IosApplicationConfigurationClient;
import com.namazustudios.socialengine.client.rest.client.PSNApplicationConfigurationClient;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;

import static com.namazustudios.socialengine.client.controlpanel.NameTokens.APPLICATION_CONFIG_FACEBOOK_EDIT;

/**
 * Created by patricktwohig on 6/21/17.
 */
public class ConfigurationUtils {

    private PlaceManager placeManager;

    private FacebookApplicationConfigurationClient facebookApplicationConfigurationClient;

    private IosApplicationConfigurationClient iosApplicationConfigurationClient;

    private GooglePlayApplicationConfigurationClient googlePlayApplicationConfigurationClient;

    private PSNApplicationConfigurationClient psnApplicationConfigurationClient;

    public void editConfiguration(final Application application,
                                  final ApplicationConfiguration applicationConfiguration) {

        final PlaceRequest.Builder placeRequestBuilder = new PlaceRequest.Builder();

        switch (applicationConfiguration.getCategory()) {
            case FACEBOOK:
                placeRequestBuilder
                    .nameToken(APPLICATION_CONFIG_FACEBOOK_EDIT)
                    .with(FacebookApplicationConfigurationEditorPresenter.Param.application_id.name(), application.getId())
                    .with(FacebookApplicationConfigurationEditorPresenter.Param.configuration_id.name(), applicationConfiguration.getId());
                break;
            default:
                throw new IllegalStateException("Not supported: " + applicationConfiguration.getCategory());
        }

        final PlaceRequest placeRequest = placeRequestBuilder.build();
        getPlaceManager().revealPlace(placeRequest);

    }

    public DeleteOperation deleteConfiguration(final ApplicationConfiguration applicationConfiguration) {

        switch (applicationConfiguration.getCategory()) {

            case FACEBOOK:
                return getFacebookApplicationConfigurationClient()::deleteApplicationConfiguration;

            case IOS_APP_STORE:
                return getIosApplicationConfigurationClient()::deleteApplicationConfiguration;

            case PSN_PS4:
            case PSN_VITA:
                return getPsnApplicationConfigurationClient()::deleteApplicationConfiguration;

            case ANDROID_GOOGLE_PLAY:
                return getGooglePlayApplicationConfigurationClient()::deleteApplicationConfiguration;

            default:
                return (applicationNameOrId, applicationConfigurationNameOrId, voidMethodCallback) -> {
                    voidMethodCallback.onSuccess(null, null);
                    Notify.notify("TODO: Not implemented for platform: " + applicationConfiguration.getCategory());
                };

        }
    }

    public PlaceManager getPlaceManager() {
        return placeManager;
    }

    @Inject
    public void setPlaceManager(PlaceManager placeManager) {
        this.placeManager = placeManager;
    }

    public FacebookApplicationConfigurationClient getFacebookApplicationConfigurationClient() {
        return facebookApplicationConfigurationClient;
    }

    @Inject
    public void setFacebookApplicationConfigurationClient(FacebookApplicationConfigurationClient facebookApplicationConfigurationClient) {
        this.facebookApplicationConfigurationClient = facebookApplicationConfigurationClient;
    }

    public IosApplicationConfigurationClient getIosApplicationConfigurationClient() {
        return iosApplicationConfigurationClient;
    }

    @Inject
    public void setIosApplicationConfigurationClient(IosApplicationConfigurationClient iosApplicationConfigurationClient) {
        this.iosApplicationConfigurationClient = iosApplicationConfigurationClient;
    }

    public GooglePlayApplicationConfigurationClient getGooglePlayApplicationConfigurationClient() {
        return googlePlayApplicationConfigurationClient;
    }

    @Inject
    public void setGooglePlayApplicationConfigurationClient(GooglePlayApplicationConfigurationClient googlePlayApplicationConfigurationClient) {
        this.googlePlayApplicationConfigurationClient = googlePlayApplicationConfigurationClient;
    }

    public PSNApplicationConfigurationClient getPsnApplicationConfigurationClient() {
        return psnApplicationConfigurationClient;
    }

    @Inject
    public void setPsnApplicationConfigurationClient(PSNApplicationConfigurationClient psnApplicationConfigurationClient) {
        this.psnApplicationConfigurationClient = psnApplicationConfigurationClient;
    }

    interface DeleteOperation {

        void perform(String applicationNameOrId,
                     String applicationConfigurationNameOrId,
                     MethodCallback<Void> voidMethodCallback);

    }

}
