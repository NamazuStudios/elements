package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.IosApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.application.ProductBundleReward;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import jakarta.inject.Inject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoFacebookApplicationConfigurationDaoTest extends MongoApplicationConfigurationDaoTest<FacebookApplicationConfiguration> {

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
    protected Class<FacebookApplicationConfiguration> getTestType() {
        return FacebookApplicationConfiguration.class;
    }

    @Override
    protected FacebookApplicationConfiguration createTestObject() {
        final var config = new FacebookApplicationConfiguration();
        config.setApplicationId("com.example.myapplication");
        config.setApplicationSecret("example-secret");
        config.setProductBundles(List.of());
        return config;
    }

    @Override
    protected FacebookApplicationConfiguration updateTestObject(final FacebookApplicationConfiguration config) {
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

        config.setApplicationId("com.example.myapplication.update");
        config.setApplicationSecret("example-secret-update");
        config.setProductBundles(List.of(bundle));

        return config;
    }

    @Override
    protected void assertCreatedCorrectly(
            final FacebookApplicationConfiguration actual,
            final FacebookApplicationConfiguration expected) {
        assertNull(actual.getBuiltinApplicationPermissions());
        assertEquals(actual.getApplicationId(), expected.getApplicationId());
        assertEquals(actual.getApplicationSecret(), expected.getApplicationSecret());
        Assert.assertEquals(actual.getProductBundles(), expected.getProductBundles());
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
            final FacebookApplicationConfiguration intermediate) {

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
                FacebookApplicationConfiguration.class,
                List.of(bundle)
        );

        intermediates.put(intermediate.getId(), result);

    }

}
