package dev.getelements.elements.service.blockchain.invoke.near;

import com.syntifi.near.api.rpc.NearClient;
import dev.getelements.elements.model.blockchain.contract.NearInvokeContractResponse;
import dev.getelements.elements.service.NearSmartContractInvocationService;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

public class NearInvoker implements NearSmartContractInvocationService.Invoker {

    private static final Logger logger = LoggerFactory.getLogger(NearInvoker.class);

    private NearInvocationScope nearInvocationScope;

    private NearClient nearClient;

    @Override
    public void initialize(final NearInvocationScope nearInvocationScope) {
        this.nearInvocationScope = nearInvocationScope;
    }

    @Override
    public NearInvokeContractResponse send(
            final String script,
            final List<String> argumentTypes,
            final List<?> arguments) {

        if (nearInvocationScope.getWalletAccount().isEncrypted()) {
            throw new IllegalStateException("Wallet must be decrypted.");
        }

        //TODO: write send logic
        return new NearInvokeContractResponse();
    }

    @Override
    public Object call(final String script, final List<String> argumentTypes, final List<?> arguments) {
        //TODO: write call logic
        return null;
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

}
