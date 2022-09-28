package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_HTTP_PROTOCOL;
import static com.namazustudios.socialengine.rt.transact.SimpleJsonRpcManifestTestHappy.HAPPY_SCOPE;

@RemoteModel(scopes = @RemoteScope(scope = HAPPY_SCOPE, protocol = ELEMENTS_JSON_RPC_HTTP_PROTOCOL))
public class TestJsonRpcModelA {}
