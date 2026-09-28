package dev.getelements.elements.service;

import dev.getelements.elements.sdk.dao.TotpConfigurationDao;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateTotpConfigurationRequest;
import dev.getelements.elements.sdk.model.auth.TotpConfiguration;
import dev.getelements.elements.service.auth.totp.SuperUserTotpConfigurationService;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SuperUserTotpConfigurationServiceTest {

    private TotpConfigurationDao totpConfigurationDao;

    private SuperUserTotpConfigurationService service;

    @BeforeMethod
    public void setup() {
        totpConfigurationDao = mock(TotpConfigurationDao.class);
        service = new SuperUserTotpConfigurationService();
        service.setTotpConfigurationDao(totpConfigurationDao);
    }

    @Test
    public void testGetConfigurationDelegatesToDao() {
        final var expected = new TotpConfiguration();
        expected.setEnabled(true);
        when(totpConfigurationDao.getConfiguration()).thenReturn(expected);
        assertEquals(service.getConfiguration(), expected);
    }

    @Test
    public void testUpdateConfigurationPropagatesEnabledFlag() {

        when(totpConfigurationDao.updateConfiguration(any())).thenAnswer(i -> i.getArgument(0));

        final var request = new CreateOrUpdateTotpConfigurationRequest();
        request.setEnabled(true);

        final var result = service.updateConfiguration(request);

        final var captor = ArgumentCaptor.forClass(TotpConfiguration.class);
        verify(totpConfigurationDao).updateConfiguration(captor.capture());

        assertTrue(captor.getValue().isEnabled());
        assertTrue(result.isEnabled());

    }

}
