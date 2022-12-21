package org.example.controllers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestinationLoader;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultHttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;

import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartner;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartnerAddress;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessPartnerService;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BusinessPartnerControllerTest
{
    private static final HttpDestination DESTINATION =
        DefaultHttpDestination.builder("http://localhost/").name("TestDestination").build();

    @Before
    public void setupDestinationAccessor() {
        DestinationAccessor.setLoader(new DefaultDestinationLoader().registerDestination(DESTINATION));
    }

    @After
    public void resetDestinationAccessor() {
        DestinationAccessor.setLoader(null);
    }

    @Test
    public void test() {
        BusinessPartnerController controller = new BusinessPartnerController();

        // test data
        BusinessPartnerAddress address = BusinessPartnerAddress.builder().country("Fantasyland").build();
        BusinessPartner businessPartner = BusinessPartner.builder().businessPartnerAddress(address).build();

        // mock business partner service
        controller.service = mock(DefaultBusinessPartnerService.class, Mockito.RETURNS_DEEP_STUBS);
        when(controller.service.getAllBusinessPartner()
            .select(any())
            .filter(any())
            .top(any())
            .executeRequest(argThat(DESTINATION::equals))
        ).thenReturn(Collections.singletonList(businessPartner));

        // run test and assertions
        List<BusinessPartnerAddress> result = controller.getAddressesByLastName("TestDestination", "Fizzbuzz");
        assertEquals(1, result.size());
        assertEquals(address, result.get(0));
    }
}
