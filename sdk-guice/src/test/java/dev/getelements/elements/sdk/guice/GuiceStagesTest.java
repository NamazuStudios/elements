package dev.getelements.elements.sdk.guice;

import com.google.inject.Stage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static dev.getelements.elements.sdk.guice.GuiceStages.STAGE_PROPERTY;
import static org.testng.Assert.assertEquals;

public class GuiceStagesTest {

    @AfterMethod
    public void clearProperty() {
        System.clearProperty(STAGE_PROPERTY);
    }

    @Test
    public void defaultsToDevelopmentWhenUnset() {
        assertEquals(GuiceStages.get(), Stage.DEVELOPMENT);
    }

    @Test
    public void honorsSystemProperty() {
        System.setProperty(STAGE_PROPERTY, "PRODUCTION");
        assertEquals(GuiceStages.get(), Stage.PRODUCTION);
    }

    @Test
    public void systemPropertyIsCaseInsensitive() {
        System.setProperty(STAGE_PROPERTY, "production");
        assertEquals(GuiceStages.get(), Stage.PRODUCTION);
    }

    @Test
    public void fallsBackToDevelopmentOnInvalidValue() {
        System.setProperty(STAGE_PROPERTY, "not-a-real-stage");
        assertEquals(GuiceStages.get(), Stage.DEVELOPMENT);
    }

    @Test
    public void fallsBackToDevelopmentOnBlankValue() {
        System.setProperty(STAGE_PROPERTY, "   ");
        assertEquals(GuiceStages.get(), Stage.DEVELOPMENT);
    }

}
