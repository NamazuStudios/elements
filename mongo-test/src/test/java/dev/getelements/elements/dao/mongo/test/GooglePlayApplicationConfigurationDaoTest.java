package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.application.ProductBundleReward;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class GooglePlayApplicationConfigurationDaoTest extends MongoApplicationConfigurationDaoTest<GooglePlayApplicationConfiguration> {

    @Inject
    private ItemTestFactory itemTestFactory;

    private Item testItem;

    @BeforeClass
    public void setUpTestItem() {
        testItem = itemTestFactory.createTestItem(
                ItemCategory.FUNGIBLE,
                "test",
                "Integration Testing Item.",
                List.of(),
                true);
    }

    @Override
    protected Class<GooglePlayApplicationConfiguration> getTestType() {
        return GooglePlayApplicationConfiguration.class;
    }

    @Override
    protected GooglePlayApplicationConfiguration createTestObject() {
        final var config = new GooglePlayApplicationConfiguration();
        config.setApplicationId("com.example.mycompany.myapp");
        config.setProductBundles(List.of());
        return config;
    }

    @Override
    protected GooglePlayApplicationConfiguration updateTestObject(final GooglePlayApplicationConfiguration config) {

        final var reward1 = new ProductBundleReward();
        reward1.setItemId(testItem.getId());
        reward1.setQuantity(5);

        final var reward2 = new ProductBundleReward();
        reward2.setItemId(testItem.getName());
        reward2.setQuantity(15);

        final var bundle = new ProductBundle();
        bundle.setDescription("Test Bundle");
        bundle.setProductId("com.example.mycompany.myapp.iap.a");
        bundle.setProductBundleRewards(List.of(reward1, reward2));

        config.setApplicationId("com.example.test.updated");
        config.setProductBundles(List.of(bundle));

        return config;
    }

    @Override
    protected void assertCreatedCorrectly(
            final GooglePlayApplicationConfiguration actual,
            final GooglePlayApplicationConfiguration expected) {
        assertNotNull(actual.getApplicationId());
        assertEquals(actual.getApplicationId(), expected.getApplicationId());
        assertEquals(actual.getProductBundles(), expected.getProductBundles());
    }

    @Test(
            threadPoolSize = 10,
            dataProvider = "getIntermediatesByMixedScope",
            groups = "updateApplicationConfiguration",
            dependsOnGroups = "createApplicationConfiguration"
    )
    public void testUpdateProductBundles(
            final String applicationScope,
            final String applicationConfigurationScope,
            final GooglePlayApplicationConfiguration intermediate) {

        final var reward1 = new ProductBundleReward();
        reward1.setItemId(testItem.getId());
        reward1.setQuantity(15);

        final var reward2 = new ProductBundleReward();
        reward2.setItemId(testItem.getName());
        reward2.setQuantity(25);

        final var bundle = new ProductBundle();
        bundle.setDescription("Test Bundle");
        bundle.setProductId("com.example.mycompany.myapp.iap.b");
        bundle.setProductBundleRewards(List.of(reward1, reward2));

        final var result = applicationConfigurationDao.updateProductBundles(
                applicationScope,
                applicationConfigurationScope,
                GooglePlayApplicationConfiguration.class,
                List.of(bundle)
        );

        intermediates.put(intermediate.getId(), result);

    }


}
