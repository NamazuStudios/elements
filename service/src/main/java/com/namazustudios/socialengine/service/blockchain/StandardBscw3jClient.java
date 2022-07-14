package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.blockchain.bsc.*;

import javax.inject.Inject;
import javax.inject.Named;

import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.primitive.Byte;
import org.web3j.abi.datatypes.primitive.Int;
import org.web3j.abi.datatypes.primitive.Long;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.*;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class StandardBscw3jClient implements Bscw3jClient {

    private HttpService httpService;


    @Override
    public Web3j getWeb3j() {
        return Web3j.build(httpService);
    }

    @Override
    public Web3jService getWeb3jService() {
        return httpService;
    }

    @Override
    public Credentials getAccount(String privateKey) {
        return Credentials.create(privateKey);
    }

    @Override
    public Web3jWallet getWallet(BigInteger accountSecretKey) {
        return new Web3jWallet(accountSecretKey);
    }

    @Override
    public Web3jWallet createWallet(String name)  throws CipherException{
        try{
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();
            return new Web3jWallet(name, privateKeyInDec,"");
        }catch (InvalidAlgorithmParameterException e) {
            throw new CipherException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new CipherException(e.getMessage());
        } catch (NoSuchProviderException e) {
            throw new CipherException(e.getMessage());
        }
    }

    @Override
    public Web3jWallet createWallet(String name, String password) throws CipherException {
        try{
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();
            return new Web3jWallet(name, privateKeyInDec, password);
        }catch (InvalidAlgorithmParameterException e) {
            throw new CipherException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new CipherException(e.getMessage());
        } catch (NoSuchProviderException e) {
            throw new CipherException(e.getMessage());
        }
    }

    @Override
    public Web3jWallet createWallet(String name, String password, String privateKey) throws CipherException {
        final var importedAccount = Credentials.create(privateKey);
        BigInteger privateKeyInDec = importedAccount.getEcKeyPair().getPrivateKey();
        return new Web3jWallet(name, privateKeyInDec, password);
    }

    @Override
    public Web3jWallet updateWallet(Web3jWallet wallet, String name, String password, String newPassword) throws CipherException{
        if (!Web3jWallet.decrypt(wallet.getSeed()).equals(password)){
            throw new CipherException("Invalid Password.");
        }
        if (wallet != null && wallet.getAccounts() != null && wallet.getAccounts().size() > 0){
            return new Web3jWallet( name, wallet.getVersion(), newPassword,
                    new BigInteger(Web3jWallet.decrypt(wallet.getAccounts().get(0)), 16) ,  wallet.getExtra());
        }else{
            try{
                ECKeyPair ecKeyPair = Keys.createEcKeyPair();
                BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();
                return new Web3jWallet( name, wallet!=null?wallet.getVersion():null, password, privateKeyInDec,  wallet!=null?wallet.getExtra():null);
            }catch (InvalidAlgorithmParameterException e) {
                throw new CipherException(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                throw new CipherException(e.getMessage());
            } catch (NoSuchProviderException e) {
                throw new CipherException(e.getMessage());
            }
        }
    }

    @Override
    public Type convertObject(final Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof String) {
            final var str = (String)object;
            if(Numeric.containsHexPrefix(str))
                return new Address(str);
            return new Utf8String(str);
        } else if (object instanceof Integer) {
            return new Int((Integer)object);
        } else if (object instanceof Long) {
            return new Long((long) object);
        } else if (object instanceof Boolean) {
            return new Bool((Boolean) object);
        } else if (object instanceof List) {
            return convertList((List) object);
        } else {
            throw new IllegalArgumentException("Invalid object: " + object);
        }
    }

    private DynamicArray convertList(final List<Object> list) {
        final var l = list.stream()
                .map(this::convertObject)
                .collect(Collectors.toList());

        Type prev = null;
        for (Type t : l) {
            if(prev != null && !Objects.equals(t.getTypeAsString(), prev.getTypeAsString())) {
                return new DynamicStruct(l);
            }
            prev = t;
        }

        return new DynamicArray(l);
    }

    @Inject
    private void setHttpService(@Named(Constants.BSC_RPC_PROVIDER)String bscHost) {
        httpService = new HttpService(bscHost);
    }
}
