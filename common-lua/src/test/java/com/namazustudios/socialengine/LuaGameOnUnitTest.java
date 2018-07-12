package com.namazustudios.socialengine;

import com.google.inject.Inject;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.spy;

@Guice(modules = UnitTestModule.class)
public class LuaGameOnUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaGameOnUnitTest.class);

    private Context context;

    @Test(dataProvider = "resourcesToTest")
    public void performLuaTest(final String moduleName, final String methodName) {

        final Path path = new Path("socialengine-test-" + randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create(moduleName, path);

        final Profile profile = spy(Profile.class);
        profile.setDisplayName("Testy McTesterson");

        final Object result = getContext().getResourceContext().invoke(resourceId, methodName);
        logger.info("Successfuly got test result {}", result);
        getContext().getResourceContext().destroy(resourceId);

    }

    @DataProvider
    public static Object[][] resourcesToTest() {
        return new Object[][] {
            {"namazu.elements.test.auth", "test_facebook_security_manifest"},
        };
    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }


}
