package com.namazustudios.socialengine.service.blockchain.invoke.flow;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.service.FlowSmartContractInvocationService;
import com.namazustudios.socialengine.service.blockchain.invoke.ScopedInvoker;
import org.onflow.sdk.*;
import org.onflow.sdk.crypto.Crypto;
import org.onflow.sdk.crypto.PrivateKey;

import javax.inject.Inject;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.onflow.sdk.FlowTransactionStatus.EXPIRED;
import static org.onflow.sdk.FlowTransactionStatus.SEALED;
import static org.onflow.sdk.crypto.Crypto.getSigner;

public class FlowInvoker implements ScopedInvoker<FlowInvocationScope>, FlowSmartContractInvocationService.Invoker {

    private static final long POLL_RATE_MSEC = 1000l;

    private static final String SCRIPT_HEADER_FORMAT = "import %s\n\n%s";

    private FlowAccessApi flowAccessApi;

    private FlowInvocationScope flowInvocationScope;

    private FlowAddress senderFlowAddress;

    private PrivateKey senderPrivateKey;

    private FlowAddress contractFlowAddress;

    @Override
    public Object send(final String script, final List<?> arguments) {

        final var fullScript = format(SCRIPT_HEADER_FORMAT, contractFlowAddress.getFormatted(), script);

        final var flowScript = new FlowScript(fullScript);

        final var flowArguments = arguments
                .stream()
                .map(this::getFlowArgument)
                .collect(toList());

        final var latestBlockId = getFlowAccessApi()
                .getLatestBlockHeader()
                .getId();

        final var senderAccountKey = getAccountKey();

        final var flowTransactionProposalKey = new FlowTransactionProposalKey(
                senderFlowAddress,
                senderAccountKey.getId(),
                senderAccountKey.getSequenceNumber()
        );

        final var txn = new FlowTransaction(
                flowScript,
                flowArguments,
                latestBlockId,
                flowInvocationScope.getGasLimit(),
                flowTransactionProposalKey,
                senderFlowAddress,
                List.of(senderFlowAddress),
                Collections.emptyList(),
                Collections.emptyList()
        );

        final var signer = getSigner(senderPrivateKey, senderAccountKey.getHashAlgo());
        txn.addEnvelopeSignature(senderFlowAddress, senderAccountKey.getId(), signer);

        final var txnId = flowAccessApi.sendTransaction(txn);
        final var txnResult = waitForSeal(txnId);

        return null;

    }

    private FlowArgument getFlowArgument(Object o) {
        return null;
    }

    private FlowAccountKey getAccountKey() {

        final var preferredAccount = flowInvocationScope.getWallet().getPreferredAccount();
        final var flowAccount = flowAccessApi.getAccountAtLatestBlock(senderFlowAddress);

        if (flowAccount == null) {

            final var msg = format("Unable to locate flow account for: %s (%s)",
                    flowInvocationScope.getWallet().getId(),
                    senderFlowAddress
            );

            throw new InternalException(msg);

        }

        final var flowAccountKeys = flowAccount.getKeys();

        if (flowAccountKeys.size() < preferredAccount) {

            final var msg = format("Preferred account %d does not exist for: %s (%s)",
                    preferredAccount,
                    flowInvocationScope.getWallet().getId(),
                    senderFlowAddress
            );

            throw new InternalException(msg);

        }

        return flowAccountKeys.get(preferredAccount);

    }

    private FlowTransactionResult waitForSeal(final FlowId txnId) {

        long attempts = 0;

        while(true) {

            final var txnResult = flowAccessApi.getTransactionResultById(txnId);

            if (txnResult == null) {

            } else if (txnResult.getStatus().equals(SEALED)) {
                return txnResult;
            } else if (txnResult.getStatus().equals(EXPIRED)) {
                throw new InternalException("Transaction expired: " + EXPIRED);
            }

            try {
                Thread.sleep(POLL_RATE_MSEC);
            } catch (InterruptedException ex) {
                throw new InternalException("Interrupted while waiting for transaction to complete.", ex);
            }

        }
    }

    @Override
    public Object call() {
        return null;
    }

    @Override
    public void initialize(final FlowInvocationScope flowInvocationScope) {

        final var senderAddress = flowInvocationScope.getWalletAccount().getAddress();
        final var senderPrivateKey = flowInvocationScope.getWalletAccount().getPrivateKey();
        final var contractAddress = flowInvocationScope.getSmartContractAddress().getAddress();

        this.senderFlowAddress =  new FlowAddress(senderAddress);
        this.senderPrivateKey = Crypto.decodePrivateKey(senderPrivateKey);
        this.contractFlowAddress = new FlowAddress(contractAddress);

        this.flowInvocationScope = flowInvocationScope;

    }

    public FlowAccessApi getFlowAccessApi() {
        return flowAccessApi;
    }

    @Inject
    public void setFlowAccessApi(FlowAccessApi flowAccessApi) {
        this.flowAccessApi = flowAccessApi;
    }

}
