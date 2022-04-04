package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.namazustudios.socialengine.dao.BscWalletDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.Token;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.service.UserService;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.wallet.Wallet;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SuperUserBscWalletService implements BscWalletService {

    private BscWalletDao BscWalletDao;

    private User user;

    private PasswordGenerator passwordGenerator;

    private Bscw3jClient bscw3JClient;

    private UserService userService;

    private ObjectMapper objectMapper;

    @Override
    public Pagination<BscWallet> getWallets(final int offset, final int count, final String userId) {
        return getWalletDao().getWallets(offset, count, userId);
    }

    @Override
    public BscWallet getWallet(final String walletNameOrId) {
        return getWalletDao().getWallet(walletNameOrId);
    }

    @Override
    public BscWallet updateWallet(final String walletId, final UpdateBscWalletRequest walletRequest) {
        final var user = getUser();
        final var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();

        if (userId.isEmpty()) {
            walletRequest.setUserId(user.getId());
        }

        final var name = Strings.nullToEmpty(walletRequest.getDisplayName()).trim();
        final var password = Strings.nullToEmpty(walletRequest.getPassword()).trim();
        final var newPassword = Strings.nullToEmpty(walletRequest.getNewPassword()).trim();

        final var BscWallet = getWalletDao().getWallet(walletId);
        try {
            final var walletFromElements = getBscw3jClient().elementsWalletToNEP6(BscWallet.getWallet());
            final var wallet = getBscw3jClient().updateWallet(walletFromElements, name, password, newPassword);
            return getWalletDao().updateWallet(walletId, walletRequest, getBscw3jClient().nep6ToElementsWallet(wallet));
        } catch (final CipherException | NEP2InvalidFormat | NEP2InvalidPassphrase | JsonProcessingException e) {
            throw new InternalException(e.getMessage());
        }
    }

    @Override
    public BscWallet createWallet(final CreateBscWalletRequest walletRequest) {

        final var user = getUser();
        final var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();

        if (userId.isEmpty()){
            walletRequest.setUserId(user.getId());
        }

        final var existing = getWalletDao().getWalletForUser(walletRequest.getUserId(), walletRequest.getDisplayName());

        if (existing != null) {
            throw new DuplicateException(String.format("Wallet with name: %s already exists.", walletRequest.getDisplayName()));
        }

        final var pw = Strings.nullToEmpty(walletRequest.getPassword()).trim();
        final var wif = Strings.nullToEmpty(walletRequest.getPrivateKey()).trim();

        try {
            final var wallet = wif.isEmpty() ?
                    getBscw3jClient().createWallet(walletRequest.getDisplayName(), pw) :
                    getBscw3jClient().createWallet(walletRequest.getDisplayName(), pw, wif);

            final var elementsWallet = getBscw3jClient().nep6ToElementsWallet(wallet);
            final var BscWallet = new BscWallet();

            BscWallet.setDisplayName(walletRequest.getDisplayName());
            BscWallet.setWallet(elementsWallet);
            BscWallet.setUser(getUserService().getUser(walletRequest.getUserId()));

            return getWalletDao().createWallet(BscWallet);
        } catch (final CipherException | JsonProcessingException e) {
            throw new InternalException(e.getMessage());
        }
    }

    @Override
    public List<Token> getWalletNFTContents(final String walletNameOrId) {

        final var wallet = getWalletDao().getWallet(walletNameOrId);
        final var nepWallet = getBscw3jClient().elementsWalletToNEP6(wallet.getWallet());
        final var mintAccount = Wallet.fromNEP6Wallet(nepWallet).getDefaultAccount();

        final var client = getBscw3jClient().getNeow3j();
        final var getNep11BalanacesRequest = client.getNep11Balances(mintAccount.getScriptHash());

        try {

            final var rawResponse = getNep11BalanacesRequest.send();

            if(rawResponse.hasError()) {
                throw new InternalException(rawResponse.getError().getMessage());
            }

            final var response = rawResponse.getBalances();
            final var balances = response.getBalances();
            final List<Token> tokens = new ArrayList();


            for (final var balance : balances) {

                final List<ContractParameter> params = new ArrayList<>();
                params.add(ContractParameter.hash160(mintAccount.getScriptHash()));
                final var invokeResult = client.invokeFunction(balance.getAssetHash(), "tokensOf", params).send();
                final var resultIterator = invokeResult.getInvocationResult().getStack().iterator();

                while (resultIterator.hasNext()) {

                    final var interopInterface = resultIterator.next().getIterator();

                    for (final var stackItem : interopInterface) {

                        final var tokenId = stackItem.getString();
                        final var r =
                                client.invokeFunction(balance.getAssetHash(),
                                        "properties",
                                        List.of(ContractParameter.string(tokenId))).send();

                        if (!r.hasError() && r.getInvocationResult().getState() == NeoVMStateType.HALT) {

                            final var res = r.getInvocationResult();
                            final var stack = res.getStack();

                            for(final var item : stack) {

                                final var itemMap = convertStackItemMap(item.getMap());
                                final var token = getObjectMapper().convertValue(itemMap, Token.class);

                                tokens.add(token);
                            }
                        }
                    }
                }
            }

            return tokens;

        } catch (final IOException e) {
            throw new InternalException(e.getMessage());
        }
    }

    private String convertHashToAddress(String hash) {
        try {
            final var addressHash = new Hash160(hash);
            return addressHash.toAddress();
        } catch (IllegalArgumentException e) {
            return hash;
        }
    }

    private Map<String, Object> convertStackItemMap(final Map<StackItem, StackItem> stackItemMap) {

        final var objectMap = new HashMap<String, Object>();

        for(final var stackKey : stackItemMap.keySet()) {

            final var key = stackKey.getString();
            final var value = key.equals("owner") ?
                    convertHashToAddress(stackItemMap.get(stackKey).getHexString()) :
                    convertStackItem(stackItemMap.get(stackKey));

            objectMap.put(key, value);
        }

        return objectMap;
    }

    private List<Object> convertStackItemList(final List<StackItem> stackItemList) {
        return stackItemList.stream()
                .map(item -> convertStackItem(item))
                .collect(Collectors.toList());
    }

    private Object convertStackItem(StackItem stackValue) {
        final var type = stackValue.getType();

        switch (type) {
            case POINTER:
                return stackValue.getPointer();
            case BOOLEAN:
                return stackValue.getBoolean();
            case INTEGER:
                return stackValue.getInteger();
            case BYTE_STRING:
                return stackValue.getString();
            case ARRAY:
                return convertStackItemList(stackValue.getList());
            case MAP:
                return convertStackItemMap(stackValue.getMap());
            case INTEROP_INTERFACE:
                return convertStackItemList(stackValue.getIterator());
            default:
                return stackValue.getValue();
        }
    }

    @Override
    public void deleteWallet(final String walletId) {
        getWalletDao().deleteWallet(walletId);
    }

    public BscWalletDao getWalletDao() {
        return BscWalletDao;
    }

    @Inject
    public void setWalletDao(final BscWalletDao BscWalletDao) {
        this.BscWalletDao = BscWalletDao;
    }

    public UserService getUserService() {
        return userService;
    }

    @Inject
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public PasswordGenerator getPasswordGenerator() {
        return passwordGenerator;
    }

    @Inject
    public void setPasswordGenerator(final PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Bscw3jClient getBscw3jClient(){return bscw3JClient;}

    @Inject
    public void setBscw3jClient(final Bscw3jClient bscw3JClient){this.bscw3JClient = bscw3JClient;}

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
