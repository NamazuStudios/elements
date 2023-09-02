package dev.getelements.elements.service.blockchain.invoke.near;

import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.blockchain.contract.NearInvokeContractResponse;
import dev.getelements.elements.service.NearSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.invoke.ScopedInvoker;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

public class NearInvoker implements ScopedInvoker<NearInvocationScope>, NearSmartContractInvocationService.Invoker {

    private static final Logger logger = LoggerFactory.getLogger(NearInvoker.class);

    private NearInvocationScope nearInvocationScope;

    private Mapper mapper;

    private Function<String, String> contractFormatter;

    @Override
    public NearInvokeContractResponse send(
            final String script,
            final List<String> argumentTypes,
            final List<?> arguments) {

        if (nearInvocationScope.getWalletAccount().isEncrypted()) {
            throw new IllegalStateException("Wallet must be decrypted.");
        }


        throw new InternalException("Not implemented");
    }

    @Override
    public Object call(final String script, final List<String> argumentTypes, final List<?> arguments) {

        throw new InternalException("Not implemented");

    }

    @Override
    public void initialize(final NearInvocationScope nearInvocationScope) {

        throw new InternalException("Not implemented");

    }

    public NearInvocationScope getNearInvocationScope() {
        return nearInvocationScope;
    }

    @Inject
    public void setNearInvocationScope(NearInvocationScope nearInvocationScope) {
        this.nearInvocationScope = nearInvocationScope;
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

}
