package com.namazustudios.socialengine.service.guice;

import com.namazustudios.socialengine.service.blockchain.evm.SuperUserEvmSmartContractService;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

@Guice(modules = IntegrationTestModule.class)
public class TestEvmSmartContractService {

    private SuperUserEvmSmartContractService underTest;

    @Test
    public void test() {
        // TODO Build First Test
    }

    public SuperUserEvmSmartContractService getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(SuperUserEvmSmartContractService underTest) {
        this.underTest = underTest;
    }

}
