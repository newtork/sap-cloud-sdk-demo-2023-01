package org.example;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestinationLoader;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultHttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;

import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartner;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartnerAddress;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith( SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class BusinessPartnerControllerTest {
    private static final String DESTINATION_NAME = "Destination";
    private static final UUID BUSINESS_PARTNER_ID = UUID.fromString("00163e2c-7b39-1ed9-91d0-1182c32dc6ff");

    @Rule
    public final WireMockRule BACKEND_SYSTEM = new WireMockRule(wireMockConfig().dynamicPort());

    @Autowired
    private MockMvc mvc;

    @Test
    public void testGetBusinessPartnerAddresses() throws Exception {
        mockDestination();
        mockBusinessPartnerLookUp();
        mockAddressesLookUp();

        mvc.perform(MockMvcRequestBuilders.get("/bupa/addresses")
                .queryParam("destinationName", DESTINATION_NAME)
                .queryParam("partnerId", BUSINESS_PARTNER_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].Country", is("Legoland")));
    }

    @Test
    public void testGetBusinessPartnerSpeaksMyLanguage() throws Exception {
        mockDestination();
        mockBusinessPartnerLookUp();

        mvc.perform(MockMvcRequestBuilders.get("/bupa/speaksMyLanguage")
            .queryParam("destinationName", DESTINATION_NAME)
            .queryParam("partnerId", BUSINESS_PARTNER_ID.toString())
            .header("Accept-Language", "de-DE")).andExpect(status().isOk());
    }

    @SuppressWarnings( "UnstableApiUsage" )
    private void mockDestination() {
        final DefaultHttpDestination destination =
            DefaultHttpDestination.builder(BACKEND_SYSTEM.baseUrl()).name(DESTINATION_NAME).build();
        DestinationAccessor.prependDestinationLoader(new DefaultDestinationLoader().registerDestination(destination));
    }

    private void mockBusinessPartnerLookUp() {
        final BusinessPartner bp = new BusinessPartner();
        bp.setBusinessPartner("0001");
        bp.setBusinessPartnerUUID(BUSINESS_PARTNER_ID);
        String bpJson = "{\"d\":{\"results\":["+new Gson().toJson(bp)+"]}}";

        BACKEND_SYSTEM
            .stubFor(get(urlMatching("/.*A_BusinessPartner\\?\\$filter.*"))
                .willReturn(aResponse().withBody(bpJson)));
    }

    private void mockAddressesLookUp() {
        BusinessPartnerAddress addr = BusinessPartnerAddress.builder().addressID("123").country("Legoland").build();
        String addrJson = "{\"d\":{\"results\":["+new Gson().toJson(addr)+"]}}";

        BACKEND_SYSTEM
            .stubFor(get(urlMatching("/.*to_BusinessPartnerAddress.*"))
                .willReturn(aResponse().withBody(addrJson)));
    }
}
