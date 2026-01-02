package com.fulfai.sellingpartner.email;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@IfBuildProfile("dev")
@ApplicationScoped
public class InviteEmailSenderDev implements InviteEmailSender {

    @Override
    public void sendJoinRequestApprovalEmail(
            String ownerEmail,
            String companyName,
            String requesterEmail,
            String approvalLink
    ) {
        Log.infof(
                "[DEV EMAIL] To=%s | Company=%s | Requester=%s | Link=%s",
                ownerEmail, companyName, requesterEmail, approvalLink
        );
    }
}
