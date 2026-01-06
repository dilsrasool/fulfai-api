package com.fulfai.sellingpartner.email;

public interface InviteEmailSender {

    /**
     * Send join-request approval email to company owner
     *
     * @param ownerUserId   Cognito user id of owner
     * @param companyId     company id
     * @param requestId     join request id
     * @param approvalToken signed approval token
     */
    void sendJoinRequestApprovalEmail(
            String ownerUserId,
            String companyId,
            String requestId,
            String approvalToken
    );
}
