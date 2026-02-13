package com.fulfai.sellingpartner.analytics.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.fulfai.sellingpartner.analytics.service.AnalyticsService;
import com.fulfai.sellingpartner.analytics.dto.BranchDashboardDTO;

@Path("/analytics")
@Produces(MediaType.APPLICATION_JSON)
public class AnalyticsResource {

    @Inject
    AnalyticsService service;

    /* =========================================================
       COMPANY ANALYTICS
    ========================================================= */
    @GET
    @Path("/company")
    public BranchDashboardDTO company(
            @QueryParam("companyId") String companyId
    ) {
        return service.getCompanyDashboard(companyId);
    }

    /* =========================================================
       BRANCH ANALYTICS  ⭐ THIS WAS MISSING
    ========================================================= */
    @GET
    @Path("/branch")
    public BranchDashboardDTO branch(
            @QueryParam("companyId") String companyId,
            @QueryParam("branchId") String branchId
    ) {
        return service.getBranchDashboard(companyId, branchId);
    }
}
