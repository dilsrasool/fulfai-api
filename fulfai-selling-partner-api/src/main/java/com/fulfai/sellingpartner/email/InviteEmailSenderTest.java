package com.fulfai.sellingpartner.email;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;

@ApplicationScoped
@IfBuildProfile("dev|test")
public class InviteEmailSenderTest implements InviteEmailSender {

    @Override
    public void sendJoinRequestApprovalEmail(
            String ownerUserId,
            String companyId,
            String requestId,
            String joiningUserId
    ) {
        Log.infof(
                "[DEV EMAIL] Owner=%s Company=%s Request=%s User=%s",
                ownerUserId, companyId, requestId, joiningUserId
        );
    }
}
