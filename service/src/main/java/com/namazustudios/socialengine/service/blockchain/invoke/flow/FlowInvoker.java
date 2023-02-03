package com.namazustudios.socialengine.service.blockchain.invoke.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.blockchain.contract.FlowInvokeContractResponse;
import com.namazustudios.socialengine.service.FlowSmartContractInvocationService;
import com.namazustudios.socialengine.service.blockchain.invoke.ScopedInvoker;
import org.dozer.Mapper;
import org.onflow.sdk.*;
import org.onflow.sdk.cadence.*;
import org.onflow.sdk.crypto.Crypto;
import org.onflow.sdk.crypto.PrivateKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;
import static org.onflow.sdk.FlowTransactionStatus.EXPIRED;
import static org.onflow.sdk.FlowTransactionStatus.SEALED;
import static org.onflow.sdk.crypto.Crypto.getSigner;

public class FlowInvoker implements ScopedInvoker<FlowInvocationScope>, FlowSmartContractInvocationService.Invoker {

    private static final Logger logger = LoggerFactory.getLogger(FlowInvoker.class);

    private static final int POLL_ATTEMPTS = 30;

    private static final long POLL_RATE_MSEC = 1000L;

    private static final String CONTRACT_DELIMITER = ":";

    private static final Pattern CONTRACT_NAME = Pattern.compile("\\w+");

    private static final String SCRIPT_STAR_HEADER_FORMAT = "import %s\n\n%s";

    private static final String SCRIPT_SPECIFIC_HEADER_FORMAT = "import %s from %s\n\n%s";

    private static final Map<String, Function<Object, Field<?>>> ARGUMENT_CONVERTERS = Map.ofEntries(
            // Signed Integer Types
            entry("Int", s -> new IntNumberField(s.toString())),
            entry("Int8", s -> new Int8NumberField(s.toString())),
            entry("Int16", s -> new Int16NumberField(s.toString())),
            entry("Int32", s -> new Int32NumberField(s.toString())),
            entry("Int64", s -> new Int64NumberField(s.toString())),
            entry("Int128", s -> new Int128NumberField(s.toString())),
            entry("Int256", s -> new Int256NumberField(s.toString())),
            // Unsigned Integer Types
            entry("UInt8", s -> new UInt8NumberField(s.toString())),
            entry("UInt16", s -> new UInt16NumberField(s.toString())),
            entry("UInt32", s -> new UInt32NumberField(s.toString())),
            entry("UInt64", s -> new UInt64NumberField(s.toString())),
            entry("UInt128", s -> new UInt128NumberField(s.toString())),
            entry("UInt256", s -> new UInt256NumberField(s.toString())),
            // Word Types
            entry("Word8", s -> new Word8NumberField(s.toString())),
            entry("Word16", s -> new Word16NumberField(s.toString())),
            entry("Word32", s -> new Word32NumberField(s.toString())),
            entry("Word64", s -> new Word64NumberField(s.toString())),
            // Fixed Point Types
            entry("Fix64", s -> new Fix64NumberField(s.toString())),
            entry("UFix64", s -> new UFix64NumberField(s.toString())),
            // String Types
            entry("String", s -> new StringField(s.toString())),
            entry("Address", s -> new AddressField(s.toString())),
            // Logical Types
            entry("Boolean", s -> new BooleanField(Boolean.parseBoolean(s.toString())))
    );

    private FlowAccessApi flowAccessApi;

    private FlowInvocationScope flowInvocationScope;

    private FlowAddress senderFlowAddress;

    private PrivateKey senderPrivateKey;

    private Mapper mapper;

    private Function<String, String> contractFormatter;

    @Override
    public FlowInvokeContractResponse send(
            final String script,
            final List<String> argumentTypes,
            final List<?> arguments) {

        if (flowInvocationScope.getWalletAccount().isEncrypted()) {
            throw new IllegalStateException("Wallet must be decrypted.");
        }

        if (arguments.size() != argumentTypes.size()) {

            final var msg = format(
                    "Argument type array does not match argument counts (%d!=%d)",
                    arguments.size(),
                    argumentTypes.size()
            );

            throw new IllegalArgumentException(msg);

        }

        final var flowScript = new FlowScript(contractFormatter.apply(script));

        final var flowArguments = IntStream.range(0, arguments.size())
                .mapToObj(index -> getFlowArgument(argumentTypes.get(index), arguments.get(index)))
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

        final var signer = getSigner(senderPrivateKey, senderAccountKey.getHashAlgo());

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
        ).addEnvelopeSignature(senderFlowAddress, senderAccountKey.getId(), signer);

        final var txnId = flowAccessApi.sendTransaction(txn);
        return getMapper().map(waitForSeal(txnId), FlowInvokeContractResponse.class);

    }

    @Override
    public Object call(final String script, final List<?> arguments) {

        final var flowScript = new FlowScript(contractFormatter.apply(script));

        return getFlowAccessApi().executeScriptAtLatestBlock(flowScript, arguments
            .stream()
            .map(arg -> ByteString.copyFrom(arg.toString(), StandardCharsets.UTF_8))
            .collect(toList()));

    }

    private FlowArgument getFlowArgument(final String type, final Object argument) {

        final var converter = ARGUMENT_CONVERTERS.get(type);

        if (converter == null) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }

        final var field = converter.apply(argument);
        return new FlowArgument(field);

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

        for (int attempts = 0; attempts < POLL_ATTEMPTS; ++attempts) {

            final var txnResult = flowAccessApi.getTransactionResultById(txnId);

            if (txnResult == null) {
                logger.warn("Got null response from Flow while waiting on transaction.");
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

        throw new InternalException("Transaction timeout.");

    }

    @Override
    public void initialize(final FlowInvocationScope flowInvocationScope) {

        final var senderAddress = flowInvocationScope.getWalletAccount().getAddress();
        final var senderPrivateKey = flowInvocationScope.getWalletAccount().getPrivateKey();
        final var contractAddress = flowInvocationScope.getSmartContractAddress().getAddress();

        final var contractAddressSplit = contractAddress.split(CONTRACT_DELIMITER);

        if (contractAddressSplit.length == 1) {

            final var contractFlowAddress = new FlowAddress(contractAddress);

            contractFormatter = script -> format(
                    SCRIPT_STAR_HEADER_FORMAT,
                    contractFlowAddress.getFormatted(),
                    script
            );

        } else if (contractAddressSplit.length == 2 && CONTRACT_NAME.matcher(contractAddressSplit[1]).matches()) {

            final var contractFlowAddress = new FlowAddress(contractAddressSplit[0]);

            contractFormatter = script -> format(
                    SCRIPT_SPECIFIC_HEADER_FORMAT,
                    contractAddressSplit[1],
                    contractFlowAddress.getFormatted(),
                    script
            );

        } else {
            throw new IllegalArgumentException("Invalid Flow contract address.");
        }

        this.senderFlowAddress =  new FlowAddress(senderAddress);
        this.senderPrivateKey = Crypto.decodePrivateKey(senderPrivateKey);

        this.flowInvocationScope = flowInvocationScope;

    }

    public FlowAccessApi getFlowAccessApi() {
        return flowAccessApi;
    }

    @Inject
    public void setFlowAccessApi(FlowAccessApi flowAccessApi) {
        this.flowAccessApi = flowAccessApi;
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

}
