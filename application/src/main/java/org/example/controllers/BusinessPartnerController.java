package org.example.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class BusinessPartnerController {
    private static final Gson GSON = new Gson();

    @GetMapping("/bupa/addresses")
    public List<Object> getAddressesByLastName(
        @RequestParam String destinationName,
        @RequestParam String lastName,
        @RequestHeader(name=HttpHeaders.AUTHORIZATION, required=true) String authorization
    )
        throws IOException, NullPointerException, ClassCastException, JsonSyntaxException
    {
        HttpClient httpClient = HttpClientBuilder.create().build();

        Map<?,?> serviceBindings = GSON.fromJson(System.getenv("VCAP_SERVICES"), Map.class);
        Map<?,?> serviceDestination = (Map<?,?>) ((List<?>) serviceBindings.get("destination")).get(0);
        Map<?,?> serviceCredentials = (Map<?,?>) serviceDestination.get("credentials");

        String destinationServiceToken;
        {
            String destinationAuthUrl = String.format(
                "%s/oauth/token?client_id=%s&client_secret=%s&assertion=%s&grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&response_type=token",
                serviceCredentials.get("uri"),
                serviceCredentials.get("clientid"),
                serviceCredentials.get("clientsecret"),
                authorization);

            HttpPost xsuaaPost = new HttpPost(URI.create(destinationAuthUrl));
            xsuaaPost.setHeader(HttpHeaders.ACCEPT, "application/json");
            xsuaaPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

            InputStream responseStream = httpClient.execute(xsuaaPost).getEntity().getContent();
            Map<?,?> xsuaaResponse = GSON.fromJson(new InputStreamReader(responseStream), Map.class);
            destinationServiceToken = (String) xsuaaResponse.get("access_token");
        }

        Map<?,?> destinationProperties;
        {
            String destinationRequestUrl = String.format(
                "%s/destination-configuration/v1/destinations/%s",
                serviceCredentials.get("uri"),
                destinationName);

            HttpGet destinationGet = new HttpGet(URI.create(destinationRequestUrl));
            destinationGet.setHeader(HttpHeaders.AUTHORIZATION, destinationServiceToken);

            InputStream responseStream = httpClient.execute(destinationGet).getEntity().getContent();
            destinationProperties = GSON.fromJson(new InputStreamReader(responseStream), Map.class);
        }

        Map<?,?> odataResult;
        {
            String oDataRequestUrl = String.format("%s/sap/opu/odata/sap/API_BUSINESS_PARTNER"
                + "?$filter=LastName eq '%s'"
                + "&$top=100"
                + "&$expand=to_BusinessPartnerAddress"
                + "&$select=to_BusinessPartnerAddress/*",
                ((Map<?,?>) destinationProperties.get("destinationConfiguration")).get("URL"),
                lastName);

            HttpGet oDataGet = new HttpGet(URI.create(oDataRequestUrl));
            for( Object token : ((List<?>) destinationProperties.get("authTokens")) ) {
                Map<?,?> httpHeader = (Map<?,?>) (((Map<?, ?>) token).get("http_header"));
                oDataGet.setHeader((String) httpHeader.get("key"), (String) httpHeader.get("value"));
            }

            InputStream responseStream = httpClient.execute(oDataGet).getEntity().getContent();
            odataResult = GSON.fromJson(new InputStreamReader(responseStream), Map.class);
        }

        List<Object> addresses = new ArrayList<>();
        List<?> businessPartners = (List<?>) (((Map<?,?>) odataResult.get("d")).get("results"));
        for( Object businessPartner : businessPartners ) {
            addresses.add(((Map<?,?>) businessPartner).get("to_BusinessPartnerAddress"));
        }
        return addresses;
    }
}
