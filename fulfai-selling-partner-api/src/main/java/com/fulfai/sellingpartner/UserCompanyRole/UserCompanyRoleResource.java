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
     * 👥 Add a user with a role to a company
     */
    @POST
    public Response addUserToCompany(@PathParam("companyId") String companyId,
                                     @Valid UserCompanyRoleRequestDTO request) {
        // Ensure the request carries the companyId
        request.setCompanyId(companyId);
        userCompanyRoleService.addUserToCompany(request);
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * 👥 Get all users for a company
     */
    @GET
    public List<UserCompanyRoleResponseDTO> getUsersForCompany(@PathParam("companyId") String companyId) {
        return userCompanyRoleService.getUsersForCompany(companyId);
    }

    /**
     * 👥 Remove a user from a company
     */
    @DELETE
    @Path("/{userId}")
    public Response removeUserFromCompany(@PathParam("companyId") String companyId,
                                          @PathParam("userId") String userId) {
        userCompanyRoleService.removeUserFromCompany(companyId, userId);
        return Response.noContent().build();
    }
}
