package com.namazustudios.socialengine.jrpc.ethereum;

import com.namazustudios.socialengine.rt.annotation.*;

import java.util.Map;

import static com.namazustudios.socialengine.rt.annotation.CaseFormat.NATURAL;

@RemoteService(
        value = "contracts",
        scopes = @RemoteScope(
                scope = "ETHEREUM",
                protocol = RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL,
                style = @CodeStyle(
                        methodPrefix = "eth_",
                        constantCaseFormat = NATURAL,
                        parameterCaseFormat = NATURAL,
                        methodCaseFormat = NATURAL
                )
        )
)
public class EthSmartContract {

    @RemotelyInvokable
    public Object getBlockByHash(
            @Serialize("blockHash") final String blockHash,
            @Serialize("fullTransactionObjects") final boolean fullTransactionObjects) {
        return Map.of("message", "Hello World!");
    }

}
