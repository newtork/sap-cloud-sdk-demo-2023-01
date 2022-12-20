package org.example.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.requestheader.RequestHeaderAccessor;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;

import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartner;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartnerAddress;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.BusinessPartnerService;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessPartnerService;

@RestController
public class BusinessPartnerController {
    private final BusinessPartnerService service = new DefaultBusinessPartnerService();

    @GetMapping("/bupa/addresses")
    public List<BusinessPartnerAddress> getBusinessPartnerAddresses(
        @RequestParam String destinationName,
        @RequestParam UUID partnerId
    )
        throws ODataException
    {
        HttpDestination destination = DestinationAccessor.getDestination(destinationName).asHttp();

        List<BusinessPartner> matchingPartners = service
            .getAllBusinessPartner()
            .filter(BusinessPartner.BUSINESS_PARTNER_UUID.eq(partnerId))
            .top(1)
            .executeRequest(destination);

        List<BusinessPartnerAddress> result = new ArrayList<>();
        for( BusinessPartner bp : matchingPartners ) {
            result.addAll(bp.getBusinessPartnerAddressOrFetch());
        }
        return result;
    }

    @SuppressWarnings( "UnstableApiUsage" )
    @GetMapping("/bupa/speaksMyLanguage")
    public boolean getBusinessPartnerSpeaksMyLanguage(
        @RequestParam String destinationName,
        @RequestParam UUID partnerId
    ) {
        HttpDestination destination = DestinationAccessor.getDestination(destinationName).asHttp();

        List<BusinessPartner> matchingPartners = service
            .getAllBusinessPartner()
            .filter(BusinessPartner.BUSINESS_PARTNER_UUID.eq(partnerId))
            .top(1)
            .executeRequest(destination);

        if(matchingPartners.isEmpty()) {
            return false;
        }

        Collection<String> myLanguages = RequestHeaderAccessor.tryGetHeaderContainer()
            .map(headers -> headers.getHeaderValues("Accept-Language"))
            .filter(values -> !values.isEmpty())
            .getOrElse(Collections.singletonList("en"));

        for( String lang : myLanguages ) {
            if(lang.equals("*")) {
                return true;
            }
            for( final BusinessPartner partner : matchingPartners ) {
                if(lang.substring(0, 2).equals(partner.getCorrespondenceLanguage())) {
                    return true;
                }
            }
        }
        return false;
    }
}