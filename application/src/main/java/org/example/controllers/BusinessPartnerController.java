package org.example.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;

import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartner;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartnerAddress;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.BusinessPartnerService;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessPartnerService;

@RestController
public class BusinessPartnerController {
    BusinessPartnerService service = new DefaultBusinessPartnerService();

    @GetMapping("/bupa/addresses")
    public List<BusinessPartnerAddress> getAddressesByLastName(
        @RequestParam String destinationName,
        @RequestParam String lastName
    )
        throws ODataException
    {
        HttpDestination destination = DestinationAccessor.getDestination(destinationName).asHttp();

        List<BusinessPartner> matchingPartners = service
            .getAllBusinessPartner()
            .select(BusinessPartner.TO_BUSINESS_PARTNER_ADDRESS)
            .filter(BusinessPartner.LAST_NAME.eq(lastName))
            .top(100)
            .executeRequest(destination);

        List<BusinessPartnerAddress> result = new ArrayList<>();
        for( BusinessPartner bp : matchingPartners ) {
            result.addAll(bp.getBusinessPartnerAddressOrFetch());
        }
        return result;
    }
}
