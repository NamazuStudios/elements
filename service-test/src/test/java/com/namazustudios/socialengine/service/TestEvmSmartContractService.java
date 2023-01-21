package com.namazustudios.socialengine.service;

import org.testng.annotations.Factory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

@Guice(modules = UnixFSIntegrationTestModule.class)
public class TestEvmSmartContractService {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(TestEvmSmartContractService.class),
                TestUtils.getInstance().getUnixFSTest(TestEvmSmartContractService.class)
        };
    }

    private EvmSmartContractService underTest;

    @Test
    public void testSend() {
        // TODO Build First Test
    }

    public EvmSmartContractService getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(@Unscoped EvmSmartContractService underTest) {
        this.underTest = underTest;
    }

}
