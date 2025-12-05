package com.fulfai.deliverypartner.location;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/drivers/nearby")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProximitySearchResource {

    @Inject
    LocationService locationService;

    /**
     * Find nearby drivers based on lat/lng coordinates.
     * Uses geohash-based proximity search.
     */
    @POST
    public Response findNearbyDrivers(@Valid ProximitySearchDTO searchDTO) {
        List<NearbyDriverDTO> nearbyDrivers = locationService.findNearbyDrivers(searchDTO);
        return Response.ok(nearbyDrivers).build();
    }
}
