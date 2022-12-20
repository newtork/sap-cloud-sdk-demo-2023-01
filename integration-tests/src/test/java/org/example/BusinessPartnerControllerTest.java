package org.example;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Resources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestinationLoader;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultHttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
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
        mockMetadataLookUp();
        mockBusinessPartnerLookUp();
        mockAddressesLookUp();

        mvc.perform(MockMvcRequestBuilders.get("/bupa/addresses")
                .queryParam("destinationName", DESTINATION_NAME)
                .queryParam("partnerId", BUSINESS_PARTNER_ID.toString()))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetBusinessPartnerSpeaksMyLanguage() throws Exception {
        mockDestination();
        mockMetadataLookUp();
        mockBusinessPartnerLookUp();

        mvc.perform(MockMvcRequestBuilders.get("/bupa/speaksMyLanguage")
            .queryParam("destinationName", DESTINATION_NAME)
            .queryParam("partnerId", BUSINESS_PARTNER_ID.toString())
            .header("Accept-Language", "de-DE")).andExpect(status().isOk());
    }

    private void mockDestination() {
        final DefaultDestinationLoader customLoader = new DefaultDestinationLoader()
            .registerDestination(DefaultHttpDestination.builder(BACKEND_SYSTEM.baseUrl()).name(DESTINATION_NAME).build());
        DestinationAccessor.prependDestinationLoader(customLoader);
    }

    private void mockMetadataLookUp() throws IOException
    {
        String metadata = readResourceFile("service.edmx");

        BACKEND_SYSTEM
            .stubFor(get(urlMatching("/.*API_BUSINESS_PARTNER/\\$metadata"))
                .willReturn(aResponse().withBody(metadata)));
    }

    private void mockBusinessPartnerLookUp() throws IOException {
        String singleBusinessPartner = readResourceFile("single-business-partner.json");

        BACKEND_SYSTEM
            .stubFor(get(urlMatching("/.*A_BusinessPartner\\?\\$filter.*"))
                .willReturn(aResponse().withBody(singleBusinessPartner)));
    }

    private void mockAddressesLookUp() throws IOException {
        String businessPartnerAddresses = readResourceFile("business-partner-address.json");

        BACKEND_SYSTEM
            .stubFor(get(urlMatching("/.*to_BusinessPartnerAddress.*"))
                .willReturn(aResponse().withBody(businessPartnerAddresses)));
    }

    private static String readResourceFile(String fileName) throws IOException {
        return Resources.toString(
            Resources.getResource("BusinessPartnerControllerTest/" + fileName),
            StandardCharsets.UTF_8);
    }
}