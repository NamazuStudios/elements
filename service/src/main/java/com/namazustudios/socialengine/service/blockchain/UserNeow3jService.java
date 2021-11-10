package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.nep6.NEP6Wallet;

import javax.inject.Inject;
import javax.inject.Named;

import static java.lang.String.format;

public class UserNeow3jService implements Neow3jService{

    private Neow3j neow3j;

    private String neoHost;

    private String neoPort;

    @Override
    public Neow3j getNeow3j() {
        if (neow3j == null){
            neow3j = Neow3j.build(new HttpService(format("%s:%s", getNeoHost(), getNeoPort())));
        }
        return neow3j;
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

    private String getNeoHost() {
        return neoHost;
    }

    @Inject
    private void setNeoHost(@Named(Constants.NEO_BLOCKCHAIN_HOST)String neoHost) {
        this.neoHost = neoHost;
    }

    private String getNeoPort() {
        return neoPort;
    }

    @Inject
    private void setNeoPort(@Named(Constants.NEO_BLOCKCHAIN_PORT)String neoPort) {
        this.neoPort = neoPort;
    }
}
