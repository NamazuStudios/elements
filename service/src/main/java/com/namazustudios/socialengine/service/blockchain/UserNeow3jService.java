package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.nep6.NEP6Wallet;

public class UserNeow3jService implements Neow3jService{

    private Neow3j neow3j;

    @Override
    public Neow3j getNeow3j() {
        if (neow3j == null){
            neow3j = Neow3j.build(new HttpService("http://127.0.0.1:50012"));
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
    public ScriptBuilder getScriptBuilder() {
        return new ScriptBuilder();
    }

    @Override
    public TransactionBuilder getTransactionBuilder(Neow3j neow3j) {
        return new TransactionBuilder(neow3j);
    }
}
