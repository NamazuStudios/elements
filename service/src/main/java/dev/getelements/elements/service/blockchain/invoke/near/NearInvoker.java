package dev.getelements.elements.service.blockchain.invoke.near;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syntifi.near.api.common.exception.NearException;
import com.syntifi.near.api.common.model.common.EncodedHash;
import com.syntifi.near.api.common.model.key.PrivateKey;
import com.syntifi.near.api.common.model.key.PublicKey;
import com.syntifi.near.api.rpc.NearClient;
import com.syntifi.near.api.rpc.model.contract.ContractFunctionCallResult;
import com.syntifi.near.api.rpc.model.identifier.Finality;
import com.syntifi.near.api.rpc.model.transaction.*;
import com.syntifi.near.api.rpc.service.TransactionService;
import com.syntifi.near.borshj.Borsh;
import com.syntifi.near.borshj.BorshBuffer;
import dev.getelements.elements.sdk.model.blockchain.contract.near.*;
import dev.getelements.elements.sdk.service.blockchain.NearSmartContractInvocationService;
import dev.getelements.elements.sdk.service.blockchain.invoke.near.NearInvocationScope;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
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
    public NearInvokeContractResponse send(final List<Map.Entry<String, Map<String, Object>>> actions) {

        if (getNearInvocationScope().getWalletAccount().isEncrypted()) {
            throw new IllegalStateException("Wallet must be decrypted.");
        }

        final var contract = getNearInvocationScope().getSmartContract();

        if (contract == null) {
            throw new IllegalStateException("No contract was specified in the invocation scope.");
        }

        final var receiverId = contract.getAddresses()
                .get(getNearInvocationScope().getBlockchainNetwork())
                .getAddress();

        return sendDirect(receiverId, actions);
    }

    @Override
    public NearInvokeContractResponse sendDirect(
            final String receiverId,
            final List<Map.Entry<String, Map<String, Object>>> actions) {

        if (getNearInvocationScope().getWalletAccount().isEncrypted()) {
            throw new IllegalStateException("Wallet must be decrypted.");
        }

        try {

            final var signerIdJson = getNearInvocationScope().getWalletAccount().getAddress();
            final var signerPublicKey = PublicKey.getPublicKeyFromJson(signerIdJson);
            final var signerPrivateKey = PrivateKey.getPrivateKeyFromJson(getNearInvocationScope().getWalletAccount().getPrivateKey());

            //Near addresses/ids are public keys encoded to hex
            //See https://docs.near.org/concepts/basics/accounts/creating-accounts
            final var signerId = Hex.encodeHexString(signerPublicKey.getData());

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

        } catch (JsonProcessingException e) {
            //Failed decoding signer address into public key
            throw new IllegalStateException("Action is malformed:\n" + e.getMessage());
        } catch (GeneralSecurityException e) {
            //Failed sending transaction
            throw new IllegalStateException("Sending the transaction failed.");
        } catch (NoSuchFieldException e) {
            //Failed converting actions
            throw new IllegalStateException(e.getMessage());
        } catch (NearException e) {
            //NEAR network error
            throw e;
        }
    }

    @Override
    public NearContractFunctionCallResult call(final String accountId, final String methodName, final Map<String, ?> arguments) {

        try {

            final var stringArgs = getObjectMapper().writeValueAsString(arguments);
            final var encodedArgs = Base64.getEncoder().encodeToString(stringArgs.getBytes());
            final var response = getNearClient().callContractFunction(Finality.FINAL, accountId, methodName, encodedArgs);

            return convertNearCallContractFunctionResult(response);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("There was an error processing your function call arguments:\n" + e.getMessage());
        }
    }

    private NearContractFunctionCallResult convertNearCallContractFunctionResult(ContractFunctionCallResult callContractFunctionResult) {
        final var nearContractFunctionCallResult = new NearContractFunctionCallResult();

        nearContractFunctionCallResult.setResult(callContractFunctionResult.getResult());
        nearContractFunctionCallResult.setBlockHeight(callContractFunctionResult.getBlockHeight());
        nearContractFunctionCallResult.setResult(callContractFunctionResult.getResult());
        nearContractFunctionCallResult.setLogs(callContractFunctionResult.getLogs());
        nearContractFunctionCallResult.setBlockHash(convertNearEncodedHash(callContractFunctionResult.getBlockHash()));
        nearContractFunctionCallResult.setError(callContractFunctionResult.getError());

        return nearContractFunctionCallResult;
    }

    private NearInvokeContractResponse convertTransactionResponse(final TransactionAwait transaction) {
        final var nearStatus = convertStatus(transaction.getStatus());
        final var nearTransactionOutcome = convertTransactionOutcome(transaction.getTransactionOutcome());
        final var nearReceiptOutcome = transaction.getReceiptsOutcome()
                .stream()
                .map(r -> convertReceiptOutcome(r))
                .collect(Collectors.toList());

        return new NearInvokeContractResponse(nearStatus, nearTransactionOutcome, nearReceiptOutcome);
    }

    private NearTransactionOutcome convertTransactionOutcome(final TransactionOutcome transactionOutcome) {
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

    private NearReceiptOutcome convertReceiptOutcome(final ReceiptOutcome receiptOutcome) {
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

    private NearStatus convertStatus(final Status status) {
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

    private NearProof convertProof(final Proof proof) {
        final var nearProof = new NearProof();

        nearProof.setHash(convertNearEncodedHash(proof.getHash()));
        nearProof.setDirection(NearProof.Direction.valueOf(proof.getDirection().getName()));

        return nearProof;
    }

    private NearEncodedHash convertNearEncodedHash(final EncodedHash encodedHash) {
        final var nearEncodedHash = new NearEncodedHash();
        nearEncodedHash.setEncodedHash(encodedHash.getEncodedHash());
        return nearEncodedHash;
    }

    private NearOutcome convertOutcome(final Outcome outcome) {
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

    private NearMetadata convertMetadata(final Metadata metadata) {
        final var nearMetadata = new NearMetadata();
        nearMetadata.setVersion(metadata.getVersion());
        nearMetadata.setGasProfile(metadata.getGasProfile()
                .stream()
                .map(g -> convertGasProfile(g))
                .collect(Collectors.toList()));

        return nearMetadata;
    }

    private NearGasProfile convertGasProfile(final GasProfile gasProfile) {
        final var nearGasProfile = new NearGasProfile();
        nearGasProfile.setGasUsed(gasProfile.getGasUsed());
        nearGasProfile.setCostCategory(gasProfile.getCostCategory());
        nearGasProfile.setCost(NearCostType.valueOf(gasProfile.getCost().name()));
        return nearGasProfile;
    }

    //Converts actions and checks for valid action names:
    //https://docs.near.org/concepts/basics/transactions/overview#action
    private List<Action> convertActions(final List<Map.Entry<String, Map<String, Object>>> actions) throws NoSuchFieldException, JsonProcessingException {

        final List<Action> convertedActions = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : actions) {
            convertedActions.add(convertAction(entry));
        }

        return convertedActions;
    }

    private Action convertAction(final Map.Entry<String, Map<String, Object>> entry) throws NoSuchFieldException, JsonProcessingException {

        final var actionName = entry.getKey().replace("_", "").toLowerCase().trim();
        final var actionJson = objectMapper.writeValueAsString(entry.getValue());
        final var actionJsonBytes = Borsh.serialize(entry.getValue());

        switch (actionName) {
            case "functioncall":
                return Borsh.deserialize(BorshBuffer.wrap(actionJson.getBytes()), FunctionCallAction.class);
            case "transfer":
                return Borsh.deserialize(BorshBuffer.wrap(actionJsonBytes), TransferAction.class);
            case "deploycontract":
                return Borsh.deserialize(BorshBuffer.wrap(actionJsonBytes), DeployContractAction.class);
            case "createaccount":
                return Borsh.deserialize(BorshBuffer.wrap(actionJsonBytes), CreateAccountAction.class);
            case "deleteaccount":
                return Borsh.deserialize(BorshBuffer.wrap(actionJsonBytes), DeleteAccountAction.class);
            case "addkey":
                return Borsh.deserialize(BorshBuffer.wrap(actionJsonBytes), AddKeyAction.class);
            case "deletekey":
                return Borsh.deserialize(BorshBuffer.wrap(actionJsonBytes), DeleteKeyAction.class);
            case "stake":
                return Borsh.deserialize(BorshBuffer.wrap(actionJsonBytes), StakeAction.class);
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
