package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.model.blockchain.contract.FlowInvokeContractResponse;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.service.util.ServicesMapperRegistryProvider;
import org.onflow.sdk.FlowTransactionResult;
import org.onflow.sdk.FlowTransactionStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Regression test: FlowInvoker maps the Flow SDK's FlowTransactionResult into
 * FlowInvokeContractResponse through the shared MapperRegistry. The required mapping was never
 * registered, so every smart-contract invocation failed with "No mapping exists from
 * FlowTransactionResult to FlowInvokeContractResponse".
 */
public class FlowInvokeContractResponseMapperTest {

    private MapperRegistry mapperRegistry;

    @BeforeMethod
    public void setUp() {

        final var provider = new ServicesMapperRegistryProvider();
        provider.setApiOutsideUrlProvider(() -> "http://localhost:8080/api/rest");

        mapperRegistry = provider.get();

    }

    @Test
    public void flowTransactionResultMapsToFlowInvokeContractResponse() {

        final var transactionResult = new FlowTransactionResult(
                FlowTransactionStatus.SEALED, 0, "", List.of());

        final var response = mapperRegistry.map(transactionResult, FlowInvokeContractResponse.class);

        assertNotNull(response);
        assertEquals(response.getStatus(), FlowTransactionStatus.SEALED.name());
        assertEquals(response.getStatusCode(), 0);

    }

}
