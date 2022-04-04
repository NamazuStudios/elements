package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.blockchain.bsc.*;
import io.neow3j.contract.SmartContract;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.utils.Async;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Contract;
import io.neow3j.wallet.nep6.NEP6Wallet;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class StandardBscw3jClient implements Bscw3jClient {

    private HttpService httpService;

    private final ScheduledExecutorService executorService = Async.defaultExecutorService();

    private final Neow3jConfig neow3jConfig = new Neow3jConfig().setScheduledExecutorService(executorService);

    private final ThreadLocal<Neow3j> neow3jThreadLocal = ThreadLocal.withInitial(() -> Neow3j.build(httpService, neow3jConfig));

    @Override
    public Neow3j getNeow3j() {
        return neow3jThreadLocal.get();
    }

    @Override
    public Neow3jService getNeow3jService() {
        return httpService;
    }

    @Override
    public Account getAccount(String wif) {
        return Account.fromWIF(wif);
    }

    @Override
    public NEP6Wallet getWallet(Account account) {
        return Wallet.withAccounts(account).toNEP6Wallet();
    }

    @Override
    public NEP6Wallet createWallet(String name) throws CipherException {
        return Wallet.create("").name(name).toNEP6Wallet();
    }

    @Override
    public NEP6Wallet createWallet(String name, String password) throws CipherException {
        return Wallet.create(password).name(name).toNEP6Wallet();
    }

    @Override
    public NEP6Wallet createWallet(String name, String password, String privateKey) throws CipherException {
        final var importedAccount = Account.fromWIF(privateKey);
        final var wallet = Wallet.withAccounts(importedAccount).name(name);

        wallet.encryptAllAccounts(password);

        return wallet.toNEP6Wallet();
    }

    @Override
    public NEP6Wallet updateWallet(NEP6Wallet wallet, String name, String password, String newPassword) throws CipherException, NEP2InvalidPassphrase, NEP2InvalidFormat {
        var w = Wallet.fromNEP6Wallet(wallet);
        if (!name.isEmpty()) {
            w.name(name);
        }
        if (!password.isEmpty() && !newPassword.isEmpty()) {
            w.decryptAllAccounts(password);
            w.encryptAllAccounts(newPassword);
        }
        return w.toNEP6Wallet();
    }

    @Override
    public ScriptBuilder getScriptBuilder() {
        return new ScriptBuilder();
    }

    @Override
    public TransactionBuilder getTransactionBuilder(Neow3j neow3j) {
        return new TransactionBuilder(neow3j);
    }

    @Override
    public NEP6Wallet elementsWalletToNEP6(Nep6Wallet wallet) {
        var scryptParams = new io.neow3j.crypto.ScryptParams(wallet.getScrypt().getN(), wallet.getScrypt().getR(), wallet.getScrypt().getP());
        List<NEP6Account> nep6Accounts = new ArrayList<>();
        for (Nep6Account acc : wallet.getAccounts()) {
            List<NEP6Contract.NEP6Parameter> nep6Parameters = new ArrayList<>();
            for (Nep6Parameter param : acc.getContract().getParameters()) {
                var paramType = io.neow3j.types.ContractParameterType.fromJsonValue(param.getParamType().jsonValue());
                var nep6Parameter = new NEP6Contract.NEP6Parameter(param.getParamName(), paramType);
                nep6Parameters.add(nep6Parameter);
            }
            var nep6Contract = new NEP6Contract(acc.getContract().getScript(), nep6Parameters, acc.getContract().getDeployed());
            var nep6Account = new NEP6Account(acc.getAddress(), acc.getLabel(), acc.getIsDefault(), acc.getIsLocked(), acc.getKey(), nep6Contract, acc.getExtra());
            nep6Accounts.add(nep6Account);
        }
        return new NEP6Wallet(wallet.getName(), wallet.getVersion(), scryptParams, nep6Accounts, wallet.getExtra());
    }

    @Override
    public Nep6Wallet nep6ToElementsWallet(NEP6Wallet wallet) {
        var scryptParams = new ScryptParams(wallet.getScrypt().getN(), wallet.getScrypt().getR(), wallet.getScrypt().getP());
        List<Nep6Account> nep6Accounts = new ArrayList<>();
        for (NEP6Account acc : wallet.getAccounts()) {
            List<Nep6Parameter> nep6Parameters = new ArrayList<>();
            for (NEP6Contract.NEP6Parameter param : acc.getContract().getParameters()) {
                var paramType = ContractParameterType.fromJsonValue(param.getParamType().jsonValue());
                var nep6Parameter = new Nep6Parameter(param.getParamName(), paramType);
                nep6Parameters.add(nep6Parameter);
            }
            var nep6Contract = new Nep6Contract(acc.getContract().getScript(), nep6Parameters, acc.getContract().getDeployed());
            var nep6Account = new Nep6Account(acc.getAddress(), acc.getLabel(), acc.getDefault(), acc.getLock(), acc.getKey(), nep6Contract, acc.getExtra());
            nep6Accounts.add(nep6Account);
        }
        return new Nep6Wallet(wallet.getName(), wallet.getVersion(), scryptParams, nep6Accounts, wallet.getExtra());
    }

    @Override
    public SmartContract getSmartContract(String hash) {
        var hash160 = new Hash160(hash);
        return new SmartContract(hash160, getNeow3j());
    }

    @Override
    public ContractParameter convertObject(final Object object) {
        if (object == null) {
            return ContractParameter.any(null);
        } else if (object instanceof String) {
            try{
                var hash = new Hash160((String)object);
                return ContractParameter.hash160(hash);
            } catch(Exception ignored) {
                return ContractParameter.string((String)object);
            }
        } else if (object instanceof Integer) {
            return ContractParameter.integer((Integer)object);
        } else if (object instanceof Long) {
            return ContractParameter.integer(BigInteger.valueOf((long) object));
        } else if (object instanceof Boolean) {
            return ContractParameter.bool((Boolean) object);
        }  else if (object instanceof Map) {
            return ContractParameter.map(convertMap((Map<?,?>)object));
        } else if (object instanceof List) {
            return ContractParameter.array(convertList((List<?>) object));
        }else {
            throw new IllegalArgumentException("Invalid object: " + object);
        }
    }

    private List<ContractParameter> convertList(final List<?> list) {
        return list
                .stream()
                .map(this::convertObject)
                .collect(toList());
    }

    private Map<ContractParameter, ContractParameter> convertMap(final Map<?, ?> map) {
        return map
                .entrySet()
                .stream()
                .collect(toMap(e -> convertObject(e.getKey()), e -> convertObject(e.getValue())));
    }

    @Inject
    private void setHttpService(@Named(Constants.BSC_BLOCKCHAIN_HOST)String bscHost,
                                @Named(Constants.BSC_BLOCKCHAIN_PORT)String bscPort) {
        httpService = new HttpService(format("%s:%s", bscHost, bscPort));
    }
}
