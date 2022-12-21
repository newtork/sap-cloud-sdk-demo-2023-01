package org.example;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
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
    private static final String LAST_NAME = "FooBar";

    @Rule
    public final WireMockRule BACKEND_SYSTEM = new WireMockRule(wireMockConfig().dynamicPort());

    @Autowired
    private MockMvc mvc;

    @Test
    public void testGetBusinessPartnerAddresses() throws Exception {
        mockDestination();
        mockBusinessPartnerLookUp();

        mvc.perform(MockMvcRequestBuilders.get("/bupa/addresses")
                .queryParam("destinationName", DESTINATION_NAME)
                .queryParam("lastName", LAST_NAME))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].Country", is("Fantasyland")));
    }

    private void mockDestination() {
        DefaultHttpDestination destination =
            DefaultHttpDestination.builder(BACKEND_SYSTEM.baseUrl()).name(DESTINATION_NAME).build();
        DestinationAccessor.prependDestinationLoader(new DefaultDestinationLoader().registerDestination(destination));
    }

    private void mockBusinessPartnerLookUp() {
        BusinessPartnerAddress address = BusinessPartnerAddress.builder().addressID("1").country("Fantasyland").build();
        BusinessPartner businessPartner = BusinessPartner.builder().businessPartnerAddress(address).build();
        String bpJson = "{\"d\":{\"results\":["+new Gson().toJson(businessPartner)+"]}}";

        BACKEND_SYSTEM.stubFor(get(urlMatching("/.*A_BusinessPartner.*")).willReturn(aResponse().withBody(bpJson)));
    }
}
