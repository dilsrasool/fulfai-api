package com.fulfai.sellingpartner.companyJoinRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRole;
import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRoleRepository;
import com.fulfai.sellingpartner.email.InviteEmailSender;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
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

    /* =========================
       LIST JOIN REQUESTS
    ========================== */

    public PaginatedResponse<CompanyJoinRequestResponseDTO> listJoinRequests(
            String companyId,
            String status,
            String nextToken,
            Integer limit
    ) {

        PaginatedResponse<CompanyJoinRequest> response =
                repository.listByCompanyAndStatus(
                        companyId,
                        status,
                        nextToken,
                        limit
                );

        return PaginatedResponse.<CompanyJoinRequestResponseDTO>builder()
                .items(
                        response.getItems().stream()
                                .map(mapper::toResponseDTO)
                                .collect(Collectors.toList())
                )
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    /* =========================
       CREATE JOIN REQUEST
    ========================== */

    public CompanyJoinRequestResponseDTO createJoinRequest(
            String companyId,
            CompanyJoinRequestCreateDTO request
    ) {

        if (repository.existsPendingRequest(
                request.getUserId(),
                companyId
        )) {
            throw new BadRequestException(
                    "A pending join request already exists"
            );
        }

        Instant now = Instant.now();

        CompanyJoinRequest joinRequest = new CompanyJoinRequest();
        joinRequest.setRequestId(UUID.randomUUID().toString());
        joinRequest.setCompanyId(companyId);
        joinRequest.setUserId(request.getUserId());
        joinRequest.setStatus(STATUS_PENDING);

        joinRequest.setRequestedAt(now);
        joinRequest.setCreatedAt(now);
        joinRequest.setUpdatedAt(now);

        // ---- GSI population (CRITICAL) ----
        joinRequest.setGsi1Pk(companyId);
        joinRequest.setGsi1Sk(STATUS_PENDING + "#" + now.toEpochMilli());

        joinRequest.setGsi2Pk(request.getUserId());
        joinRequest.setGsi2Sk(companyId);

        repository.save(joinRequest);

        notifyCompanyOwners(joinRequest);

        return mapper.toResponseDTO(joinRequest);
    }

    /* =========================
       APPROVE JOIN REQUEST
       (TRANSACTIONAL)
    ========================== */

    public void approveJoinRequest(
            String companyId,
            String requestId,
            String approvedByUserId
    ) {

        CompanyJoinRequest request =
                repository.getByCompanyAndRequestId(companyId, requestId);

        if (request == null) {
            throw new NotFoundException("Join request not found");
        }

        if (!STATUS_PENDING.equals(request.getStatus())) {
            throw new BadRequestException(
                    "Only PENDING requests can be approved"
            );
        }

        repository.approveJoinRequestTransactional(
                request,
                approvedByUserId
        );

        Log.infof(
                "Join request approved → company=%s user=%s by=%s",
                companyId,
                request.getUserId(),
                approvedByUserId
        );
    }

    /* =========================
       REJECT JOIN REQUEST
    ========================== */

    public void rejectJoinRequest(
            String companyId,
            String requestId,
            String rejectedByUserId
    ) {

        CompanyJoinRequest request =
                repository.getByCompanyAndRequestId(companyId, requestId);

        if (request == null) {
            throw new NotFoundException("Join request not found");
        }

        if (!STATUS_PENDING.equals(request.getStatus())) {
            throw new BadRequestException(
                    "Only PENDING requests can be rejected"
            );
        }

        Instant now = Instant.now();

        request.setStatus(STATUS_REJECTED);
        request.setReviewedBy(rejectedByUserId);
        request.setReviewedAt(now);
        request.setUpdatedAt(now);

        // update GSI
        request.setGsi1Sk(STATUS_REJECTED + "#" + request.getRequestedAt().toEpochMilli());

        repository.save(request);

        Log.infof(
                "Join request rejected → company=%s user=%s by=%s",
                companyId,
                request.getUserId(),
                rejectedByUserId
        );
    }

    /* =========================
       OWNER EMAIL NOTIFICATION
    ========================== */

    private void notifyCompanyOwners(CompanyJoinRequest request) {

        List<UserCompanyRole> owners =
                userCompanyRoleRepository.getCompanyOwners(
                        request.getCompanyId()
                );

        if (owners == null || owners.isEmpty()) {
            Log.warnf(
                    "No OWNER found for company=%s. Join request pending without notification.",
                    request.getCompanyId()
            );
            return;
        }

        for (UserCompanyRole owner : owners) {
            inviteEmailSender.sendJoinRequestApprovalEmail(
                    owner.getUserId(),
                    request.getCompanyId(),
                    request.getRequestId(),
                    request.getUserId()
            );
        }

        Log.infof(
                "Join request notification sent to %d owner(s) for company=%s",
                owners.size(),
                request.getCompanyId()
        );
    }

    /* =========================
       RESOURCE DELEGATORS
    ========================== */

    public void approveJoinRequest(
            String companyId,
            String requestId
    ) {
        // TODO: Replace with authenticated user (JWT / Cognito)
        approveJoinRequest(companyId, requestId, "SYSTEM");
    }

    public void rejectJoinRequest(
            String companyId,
            String requestId
    ) {
        // TODO: Replace with authenticated user (JWT / Cognito)
        rejectJoinRequest(companyId, requestId, "SYSTEM");
    }
}
