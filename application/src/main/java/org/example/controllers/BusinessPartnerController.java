package org.example.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.servlet.RequestAccessor;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;

import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartner;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartnerAddress;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.BusinessPartnerService;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessPartnerService;

@RestController
public class BusinessPartnerController {
    private final BusinessPartnerService service = new DefaultBusinessPartnerService();

    @GetMapping("/bupa/addresses")
    public List<BusinessPartnerAddress> getBusinessPartnerAddresses(@RequestParam String destinationName, @RequestParam
    UUID partnerId) {
        HttpDestination destination = DestinationAccessor.getDestination(destinationName).asHttp();

        List<BusinessPartner> matchingPartners = fetchPartnersWithId(partnerId, destination);

        if (matchingPartners.isEmpty()) {
            return Collections.emptyList();
        }

        if (matchingPartners.size() > 1) {
            throw new IllegalStateException("More than one business partner found.");
        }

        try {
            return matchingPartners.get(0).getBusinessPartnerAddressOrFetch();
        } catch (ODataException e) {
            throw new IllegalStateException("Unable to fetch business partner addresses.", e);
        }
    }

    @GetMapping("/bupa/speaksMyLanguage")
    public boolean getBusinessPartnerSpeaksMyLanguage(@RequestParam String destinationName, @RequestParam UUID partnerId) {
        HttpDestination destination = DestinationAccessor.getDestination(destinationName).asHttp();

        List<BusinessPartner> matchingPartners = fetchPartnersWithId(partnerId, destination);

        if (matchingPartners.isEmpty()) {
            return false;
        }

        if (matchingPartners.size() > 1) {
            throw new IllegalStateException("More than one business partner found.");
        }

        return businessPartnerSpeaksMyLanguage(matchingPartners.get(0));
    }

    private List fetchPartnersWithId(UUID partnerId, HttpDestination destination) {
        try {
            return service
                .getAllBusinessPartner()
                .filter(BusinessPartner.BUSINESS_PARTNER_UUID.eq(partnerId))
                .execute(destination);
        } catch ( ODataException e) {
            throw new IllegalStateException("Unable to fetch business partners.", e);
        }
    }

    private boolean businessPartnerSpeaksMyLanguage(BusinessPartner partner) {
        String correspondenceLanguage = partner.getCorrespondenceLanguage();

        return RequestAccessor
            .tryGetCurrentRequest()
            .map(request -> request.getHeaders("Accept-Language"))
            .map(values -> (List<String>) Collections.list(values))
            .filter(values -> !values.isEmpty())
            .getOrElse(Collections.singletonList("en"))
            .stream()
            .anyMatch(language -> language.equals("*")
                || language.substring(0, 2).equalsIgnoreCase(correspondenceLanguage));
    }
}