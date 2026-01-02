package com.fulfai.sellingpartner.UserCompanyRole;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/company/{companyId}/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserCompanyRoleResource {

    @Inject
    UserCompanyRoleService userCompanyRoleService;

    /**
     * 👤 Add user to company or branch
     *
     * POST /company/{companyId}/users
     * Optional: ?branchId=xxx
     *
     * Body:
     * {
     *   "email": "user@domain.com",
     *   "role": "STAFF"
     * }
     */
    @POST
    public Response addUserToCompany(
            @PathParam("companyId") String companyId,
            @QueryParam("branchId") String branchId,
            @Valid UserCompanyRoleRequestDTO request
    ) {
        request.setCompanyId(companyId);
        request.setBranchId(branchId);

        userCompanyRoleService.addUserToCompany(request);

        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * 👥 Get all users for a company
     *
     * GET /company/{companyId}/users
     */
    @GET
    public List<UserCompanyRoleResponseDTO> getUsersForCompany(
            @PathParam("companyId") String companyId
    ) {
        return userCompanyRoleService.getUsersForCompany(companyId);
    }

    /**
     * 👥 Get all users for a specific branch
     *
     * GET /company/{companyId}/users/branch/{branchId}
     */
    @GET
    @Path("/branch/{branchId}")
    public List<UserCompanyRoleResponseDTO> getUsersForBranch(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId
    ) {
        return userCompanyRoleService.getUsersForBranch(companyId, branchId);
    }

    /**
     * ❌ Remove user from company or branch (EMAIL-based)
     *
   
     */
    @DELETE
    public Response removeUserFromCompany(
            @PathParam("companyId") String companyId,
            @QueryParam("email") String email,
            @QueryParam("branchId") String branchId
    ) {
        userCompanyRoleService.removeUserFromCompanyByEmail(
                companyId,
                branchId,
                email
        );
        return Response.noContent().build();
    }

}
