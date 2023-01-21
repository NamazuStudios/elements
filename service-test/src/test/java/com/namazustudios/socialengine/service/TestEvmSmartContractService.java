package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import org.testng.annotations.Factory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Map;

@Guice(modules = UnixFSIntegrationTestModule.class)
public class TestEvmSmartContractService {

    public static final Map<BlockchainNetwork, String> CONTACT_ADDRESSES = Map.of(
        BlockchainNetwork.BSC_TEST, "0xEF5FAD1711BA258ff6a4AbB1d86D165100B87956"
    );

    public static final String TEST_WALLET_ADDRESS = "0x511663912ac4b4e55bdca865aebae6a58d2e5050";

    public static final String TEST_WALLET_PRIVATE_KEY = "71e934f755e89effbb6436cdff469c7577261a366cab94e95f16f1d29da4e685";

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
