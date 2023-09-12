package dev.getelements.elements.service.blockchain.invoke.near;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syntifi.near.api.common.model.key.PrivateKey;
import com.syntifi.near.api.common.model.key.PublicKey;
import com.syntifi.near.api.rpc.NearClient;
import com.syntifi.near.api.rpc.model.transaction.*;
import com.syntifi.near.api.rpc.service.TransactionService;
import dev.getelements.elements.model.blockchain.contract.near.*;
import dev.getelements.elements.service.NearSmartContractInvocationService;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NearInvoker implements NearSmartContractInvocationService.Invoker {

    private static final Logger logger = LoggerFactory.getLogger(NearInvoker.class);

    private NearInvocationScope nearInvocationScope;

    private NearClient nearClient;

    private ObjectMapper objectMapper;

    @Override
    public void initialize(final NearInvocationScope nearInvocationScope) {
        this.nearInvocationScope = nearInvocationScope;
    }

    @Override
    public NearInvokeContractResponse send(
            final String receiverId,
            final List<Map<String, Map<String, List<?>>>> actions) {

        if (getNearInvocationScope().getWalletAccount().isEncrypted()) {
            throw new IllegalStateException("Wallet must be decrypted.");
        }

        try {

            final var signerId = getNearInvocationScope().getWalletAccount().getAddress();
            final var signerPublicKey = PublicKey.getPublicKeyFromJson(Hex.decodeHex(signerId).toString());
            final var signerPrivateKey = PrivateKey.getPrivateKeyFromJson(getNearInvocationScope().getWalletAccount().getPrivateKey());

            //Transactions require 6 parts:
            //signerId, signerPublicKey, receiverId
            //nonceForPublicKey, actions, blockHash
            final var encodedTransaction = TransactionService.prepareTransactionForActionList(getNearClient(),
                    signerId,
                    receiverId,
                    signerPublicKey,
                    signerPrivateKey,
                    convertActions(actions));

            final var transactionAwait = getNearClient().sendTransactionAwait(encodedTransaction);
            final var response = convertTransactionResponse(transactionAwait);

            return response;

        } catch (DecoderException e) {
            //Failed decoding signer address into public key
            throw new IllegalStateException("Could not decode signer address.");
        } catch (GeneralSecurityException e) {
            //Failed sending transaction
            throw new IllegalStateException("Sending the transaction failed.");
        } catch (NoSuchFieldException e) {
            //Failed converting actions
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public Object call(final String methodName, final List<?> arguments) {
        //TODO: write call logic
        return null;
    }

    private NearInvokeContractResponse convertTransactionResponse(TransactionAwait transaction) {
        final var nearStatus = convertStatus(transaction.getStatus());
        final var nearTransactionOutcome = convertTransactionOutcome(transaction.getTransactionOutcome());
        final var nearReceiptOutcome = transaction.getReceiptsOutcome()
                .stream()
                .map(r -> convertReceiptOutcome(r))
                .collect(Collectors.toList());

        return new NearInvokeContractResponse(nearStatus, nearTransactionOutcome, nearReceiptOutcome);
    }

    private NearTransactionOutcome convertTransactionOutcome(TransactionOutcome transactionOutcome) {
        final var nearTransactionOutcome = new NearTransactionOutcome();

        final var transactionEncodedHash = new NearEncodedHash();
        transactionEncodedHash.setEncodedHash(transactionOutcome.getBlockHash().getEncodedHash());

        nearTransactionOutcome.setId(transactionOutcome.getId());
        nearTransactionOutcome.setOutcome(convertOutcome(transactionOutcome.getOutcome()));
        nearTransactionOutcome.setBlockHash(transactionEncodedHash);
        nearTransactionOutcome.setProof(transactionOutcome.getProof()
                .stream()
                .map(p -> convertProof(p))
                .collect(Collectors.toList()));

        return nearTransactionOutcome;
    }

    private NearReceiptOutcome convertReceiptOutcome(ReceiptOutcome receiptOutcome) {
        final var nearReceiptOutcome = new NearReceiptOutcome();

        final var transactionEncodedHash = new NearEncodedHash();
        transactionEncodedHash.setEncodedHash(receiptOutcome.getBlockHash().getEncodedHash());

        nearReceiptOutcome.setId(receiptOutcome.getId());
        nearReceiptOutcome.setOutcome(convertOutcome(receiptOutcome.getOutcome()));
        nearReceiptOutcome.setBlockHash(transactionEncodedHash);
        nearReceiptOutcome.setProof(receiptOutcome.getProof()
                .stream()
                .map(p -> convertProof(p))
                .collect(Collectors.toList()));

        return nearReceiptOutcome;
    }

    private NearStatus convertStatus(Status status) {
        final var nearStatus = new NearStatus();

        final var successValue = new NearSuccessValueStatus();
        successValue.setSuccessValue(status.getSuccessValue().getSuccessValue());
        nearStatus.setSuccessValue(successValue);

        final var successReceiptIdStatus = new NearSuccessReceiptIdStatus();
        successReceiptIdStatus.setSuccessReceiptId(status.getSuccessReceiptId().getSuccessReceiptId());
        nearStatus.setSuccessReceiptId(successReceiptIdStatus);

        nearStatus.setFailure(status.getFailure());

        return nearStatus;
    }

    private NearProof convertProof(Proof proof) {
        final var nearProof = new NearProof();

        final var proofEncodedHash = new NearEncodedHash();
        proofEncodedHash.setEncodedHash(proof.getHash().getEncodedHash());

        nearProof.setHash(proofEncodedHash);
        nearProof.setDirection(NearProof.Direction.valueOf(proof.getDirection().getName()));

        return nearProof;
    }

    private NearOutcome convertOutcome(Outcome outcome) {
        final var nearOutcome = new NearOutcome();

        nearOutcome.setExecutorId(outcome.getExecutorId());
        nearOutcome.setLogs(new ArrayList<>(outcome.getLogs()));
        nearOutcome.setReceiptIds(new ArrayList<>(outcome.getReceiptIds()));
        nearOutcome.setGasBurnt(outcome.getGasBurnt());
        nearOutcome.setTokensBurnt(outcome.getTokensBurnt());
        nearOutcome.setStatus(convertStatus(outcome.getStatus()));
        nearOutcome.setMetadata(convertMetadata(outcome.getMetadata()));

        return nearOutcome;
    }

    private NearMetadata convertMetadata(Metadata metadata) {
        final var nearMetadata = new NearMetadata();
        nearMetadata.setVersion(metadata.getVersion());
        nearMetadata.setGasProfile(metadata.getGasProfile()
                .stream()
                .map(g -> convertGasProfile(g))
                .collect(Collectors.toList()));

        return nearMetadata;
    }

    private NearGasProfile convertGasProfile(GasProfile gasProfile) {
        final var nearGasProfile = new NearGasProfile();
        nearGasProfile.setGasUsed(gasProfile.getGasUsed());
        nearGasProfile.setCostCategory(gasProfile.getCostCategory());
        nearGasProfile.setCost(NearCostType.valueOf(gasProfile.getCost().name()));
        return nearGasProfile;
    }

    //Converts actions and checks for valid action names:
    //https://docs.near.org/concepts/basics/transactions/overview#action
    private List<Action> convertActions(List<Map<String, Map<String, List<?>>>> actions) throws NoSuchFieldException {

        final List<Action> convertedActions = new ArrayList<>();

        for (Map<String, Map<String, List<?>>> action : actions) {

            for (Map.Entry<String, Map<String, List<?>>> entry : action.entrySet()) {

                final var actionName = entry.getKey().replace("_", "").toLowerCase().trim();

                switch (actionName) {
                    case "functioncall":
                        objectMapper.convertValue(entry.getValue(), FunctionCallAction.class);
                        break;
                    case "transfer":
                        objectMapper.convertValue(entry.getValue(), TransferAction.class);
                        break;
                    case "deploycontract":
                        objectMapper.convertValue(entry.getValue(), DeployContractAction.class);
                        break;
                    case "createaccount":
                        objectMapper.convertValue(entry.getValue(), CreateAccountAction.class);
                        break;
                    case "deleteaccount":
                        objectMapper.convertValue(entry.getValue(), DeleteAccountAction.class);
                        break;
                    case "addkey":
                        objectMapper.convertValue(entry.getValue(), AddKeyAction.class);
                        break;
                    case "deletekey":
                        objectMapper.convertValue(entry.getValue(), DeleteKeyAction.class);
                        break;
                    case "stake":
                        objectMapper.convertValue(entry.getValue(), StakeAction.class);
                        break;
                    default:
                        throw new NoSuchFieldException(String.format("%s is not a valid action. Please use one of: \n" +
                                "FunctionCall to invoke a method on a contract (and optionally attach a budget for compute and storage)\n" +
                                "Transfer to move tokens from between accounts\n" +
                                "DeployContract to deploy a contract\n" +
                                "CreateAccount to make a new account (for a person, contract, refrigerator, etc.)\n" +
                                "DeleteAccount to delete an account (and transfer the balance to a beneficiary account)\n" +
                                "AddKey to add a key to an account (either FullAccess or FunctionCall access)\n" +
                                "DeleteKey to delete an existing key from an account\n" +
                                "Stake to express interest in becoming a validator at the next available opportunity", actionName));
                }
            }
        }

        return convertedActions;
    }

    public NearInvocationScope getNearInvocationScope() {
        return nearInvocationScope;
    }

    @Inject
    public void setNearInvocationScope(NearInvocationScope nearInvocationScope) {
        this.nearInvocationScope = nearInvocationScope;
    }

    public NearClient getNearClient() {
        return nearClient;
    }

    @Inject
    public void setNearClient(NearClient nearClient) {
        this.nearClient = nearClient;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
