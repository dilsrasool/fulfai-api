package com.fulfai.sellingpartner.companyJoinRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRole;
import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRoleRepository;
import com.fulfai.sellingpartner.email.InviteEmailSender;
import com.fulfai.sellingpartner.security.ApprovalTokenUtil;

import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class CompanyJoinRequestService {

    private static final String STATUS_PENDING  = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    @Inject
    CompanyJoinRequestRepository repository;

    @Inject
    CompanyJoinRequestMapper mapper;

    @Inject
    UserCompanyRoleRepository userCompanyRoleRepository;

    @Inject
    InviteEmailSender inviteEmailSender;

    @Inject
    SecurityIdentity securityIdentity;

    /* =========================
       LIST JOIN REQUESTS (OWNER)
    ========================== */

    public PaginatedResponse<CompanyJoinRequestResponseDTO> listJoinRequests(
            String companyId,
            String status,
            String nextToken,
            Integer limit
    ) {

        assertCurrentUserIsOwner(companyId);

        PaginatedResponse<CompanyJoinRequest> response =
                repository.listByCompanyAndStatus(
                        companyId,
                        status,
                        nextToken,
                        limit
                );

        return PaginatedResponse.<CompanyJoinRequestResponseDTO>builder()
                .items(
                        response.getItems()
                                .stream()
                                .map(mapper::toResponseDTO)
                                .collect(Collectors.toList())
                )
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    /* =========================
       CREATE JOIN REQUEST
       (AUTH USER)
    ========================== */

    public CompanyJoinRequestResponseDTO createJoinRequest(
            String companyId,
            CompanyJoinRequestCreateDTO ignored
    ) {

        String userId = securityIdentity.getPrincipal().getName();

        if (repository.existsPendingRequest(userId, companyId)) {
            throw new BadRequestException(
                    "A pending join request already exists"
            );
        }

        Instant now = Instant.now();

        CompanyJoinRequest joinRequest = new CompanyJoinRequest();
        joinRequest.setRequestId(UUID.randomUUID().toString());
        joinRequest.setCompanyId(companyId);
        joinRequest.setUserId(userId);
        joinRequest.setStatus(STATUS_PENDING);

        joinRequest.setRequestedAt(now);
        joinRequest.setCreatedAt(now);
        joinRequest.setUpdatedAt(now);

        /* =========================
           GSI VALUES (CRITICAL)
        ========================== */

        // user-company-index
        joinRequest.setUserCompany(companyId);

        // company-status-index
        joinRequest.setCompanyStatus(
                STATUS_PENDING + "#" + now.toEpochMilli()
        );

        repository.save(joinRequest);

        /* ---- Generate approval token ---- */
        String approvalToken =
                ApprovalTokenUtil.generateToken(
                        companyId,
                        joinRequest.getRequestId()
                );
        Log.debug("Notifying Comoany Owner About New Join Request");

        notifyCompanyOwners(joinRequest, approvalToken);

        return mapper.toResponseDTO(joinRequest);
    }

    /* =========================
       APPROVE JOIN REQUEST
       (OWNER via UI)
    ========================== */

    public void approveJoinRequest(
            String companyId,
            String requestId
    ) {

        assertCurrentUserIsOwner(companyId);

        approveJoinRequestInternal(
                companyId,
                requestId,
                securityIdentity.getPrincipal().getName()
        );
    }

    /* =========================
       APPROVE JOIN REQUEST
       (EMAIL TOKEN)
    ========================== */

    public void approveJoinRequestByToken(
            String companyId,
            String requestId
    ) {

        approveJoinRequestInternal(
                companyId,
                requestId,
                "EMAIL_APPROVAL"
        );
    }

    /* =========================
       INTERNAL APPROVAL LOGIC
       (IDEMPOTENT)
    ========================== */

    private void approveJoinRequestInternal(
            String companyId,
            String requestId,
            String approvedBy
    ) {

        CompanyJoinRequest request =
                repository.getByCompanyAndRequestId(companyId, requestId);

        if (request == null) {
            throw new NotFoundException("Join request not found");
        }

        if (STATUS_APPROVED.equals(request.getStatus())) {
            Log.infof(
                    "Join request already approved → company=%s request=%s",
                    companyId,
                    requestId
            );
            return;
        }

        if (!STATUS_PENDING.equals(request.getStatus())) {
            throw new BadRequestException(
                    "Only PENDING requests can be approved"
            );
        }

        /* ---- Update join request ---- */
        repository.approveJoinRequestTransactional(
                request,
                approvedBy
        );

        /* ---- Create user role ---- */
        UserCompanyRole role = new UserCompanyRole();
        role.setUserId(request.getUserId());
        role.setCompanyAndBranch(companyId, null);
        role.setRole("STAFF");

        userCompanyRoleRepository.save(role);

        Log.infof(
                "Join request APPROVED → company=%s user=%s by=%s",
                companyId,
                request.getUserId(),
                approvedBy
        );
    }

    /* =========================
       REJECT JOIN REQUEST
       (OWNER)
    ========================== */

    public void rejectJoinRequest(
            String companyId,
            String requestId
    ) {

        assertCurrentUserIsOwner(companyId);

        rejectJoinRequestInternal(
                companyId,
                requestId,
                securityIdentity.getPrincipal().getName()
        );
    }

    /* =========================
       REJECT JOIN REQUEST
       (EMAIL TOKEN)
    ========================== */

    public void rejectJoinRequestByToken(
            String companyId,
            String requestId
    ) {

        rejectJoinRequestInternal(
                companyId,
                requestId,
                "EMAIL_REJECT"
        );
    }

    /* =========================
       INTERNAL REJECTION LOGIC
       (IDEMPOTENT)
    ========================== */

    private void rejectJoinRequestInternal(
            String companyId,
            String requestId,
            String rejectedBy
    ) {

        CompanyJoinRequest request =
                repository.getByCompanyAndRequestId(companyId, requestId);

        if (request == null) {
            throw new NotFoundException("Join request not found");
        }

        if (!STATUS_PENDING.equals(request.getStatus())) {
            Log.infof(
                    "Reject ignored (already %s) → company=%s request=%s",
                    request.getStatus(),
                    companyId,
                    requestId
            );
            return;
        }

        Instant now = Instant.now();

        request.setStatus(STATUS_REJECTED);
        request.setReviewedBy(rejectedBy);
        request.setReviewedAt(now);
        request.setUpdatedAt(now);

        request.setCompanyStatus(
                STATUS_REJECTED + "#" + request.getRequestedAt().toEpochMilli()
        );

        repository.save(request);

        Log.infof(
                "Join request REJECTED → company=%s request=%s by=%s",
                companyId,
                requestId,
                rejectedBy
        );
    }

    /* =========================
       OWNER GUARD
    ========================== */

    private void assertCurrentUserIsOwner(String companyId) {

        String currentUserId =
                securityIdentity.getPrincipal().getName();

        boolean isOwner =
                userCompanyRoleRepository
                        .getCompanyOwners(companyId)
                        .stream()
                        .anyMatch(r ->
                                r.getUserId().equals(currentUserId)
                        );

        if (!isOwner) {
            throw new ForbiddenException(
                    "Only company owners can approve or reject join requests"
            );
        }
    }

    /* =========================
       EMAIL NOTIFICATION
    ========================== */

    private void notifyCompanyOwners(
            CompanyJoinRequest request,
            String approvalToken
    ) {

        List<UserCompanyRole> owners =
                userCompanyRoleRepository.getCompanyOwners(
                        request.getCompanyId()
                );

        if (owners == null || owners.isEmpty()) {
            Log.warnf(
                    "No OWNER found for company=%s",
                    request.getCompanyId()
            );
            return;
        }

        for (UserCompanyRole owner : owners) {
            inviteEmailSender.sendJoinRequestApprovalEmail(
                    owner.getUserId(),
                    request.getCompanyId(),
                    request.getRequestId(),
                    approvalToken
            );
        }

        Log.infof(
                "Join request notification sent to %d owner(s) for company=%s",
                owners.size(),
                request.getCompanyId()
        );
    }
}
