package com.fulfai.sellingpartner.email;

public interface InviteEmailSender {

    void sendJoinRequestApprovalEmail(
            String ownerUserId,
            String companyId,
            String requestId,
            String requestingUserId
    );
}
