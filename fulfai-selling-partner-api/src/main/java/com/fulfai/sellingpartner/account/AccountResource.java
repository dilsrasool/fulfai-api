package com.fulfai.sellingpartner.account;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.dto.PaginationDTO;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/company/{companyId}/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

    private static final int DEFAULT_LIMIT = 20;

    @Inject
    AccountService accountService;

    @GET
    @Path("/balance")
    public Response getLatestBalance(@PathParam("companyId") String companyId,
            @QueryParam("accountName") String accountName) {
        AccountResponseDTO account = accountService.getLatestBalance(companyId, accountName);
        if (account != null) {
            return Response.ok(account).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/history")
    public Response getAccountHistory(@PathParam("companyId") String companyId,
            @QueryParam("accountName") String accountName,
            PaginationDTO request) {
        Integer limit = request != null && request.getLimit() != null ? request.getLimit() : DEFAULT_LIMIT;
        String nextToken = request != null ? request.getNextToken() : null;
        PaginatedResponse<AccountResponseDTO> history = accountService.getAccountHistory(
                companyId, accountName, nextToken, limit);
        return Response.ok(history).build();
    }
}
