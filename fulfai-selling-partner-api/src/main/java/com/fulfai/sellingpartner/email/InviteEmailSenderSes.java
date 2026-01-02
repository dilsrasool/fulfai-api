package com.fulfai.sellingpartner.email;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.ses.SesClient;

@ApplicationScoped
@IfBuildProfile("prod")
public class InviteEmailSenderSes implements InviteEmailSender {

    @Inject
    SesClient sesClient;

    @Override
    public void sendJoinRequestApprovalEmail(
            String ownerUserId,
            String companyId,
            String requestId,
            String joiningUserId
    ) {
        // TODO: real SES send logic
    }
}
