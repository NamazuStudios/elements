package dev.getelements.elements.service.blockchain.omni;

import dev.getelements.elements.sdk.model.blockchain.wallet.Wallet;
import org.onflow.sdk.FlowAccessApi;

import jakarta.inject.Inject;
import java.util.function.Consumer;

public class FlowCreateWalletLifecycle implements Consumer<Wallet> {

    private FlowAccessApi flowAccessApi;

    @Override
    public void accept(final Wallet wallet) {

    }

    public FlowAccessApi getFlowAccessApi() {
        return flowAccessApi;
    }

    @Inject
    public void setFlowAccessApi(FlowAccessApi flowAccessApi) {
        this.flowAccessApi = flowAccessApi;
    }

}
