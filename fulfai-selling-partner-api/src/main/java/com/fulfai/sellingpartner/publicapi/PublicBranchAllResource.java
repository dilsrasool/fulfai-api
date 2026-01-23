package com.fulfai.sellingpartner.publicapi;

import java.util.List;

import com.fulfai.sellingpartner.branch.BranchService;
import com.fulfai.sellingpartner.publicapi.dto.PublicBranchDTO;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/public/branches/all")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicBranchAllResource {

    @Inject
    BranchService branchService;

    @GET
    public List<PublicBranchDTO> getAllActiveBranches() {
        // you already have this service method
        return branchService.getAllActiveBranchesAcrossAllCompanies()
                .stream()
                .map(b -> {
                    PublicBranchDTO dto = new PublicBranchDTO();
                    dto.id = b.getBranchId();
                    dto.companyId = b.getCompanyId();
                    dto.name = b.getName();
                    dto.address = b.getAddress();
                    dto.isActive = b.getIsActive();
                    return dto;
                })
                .toList();
    }
}
