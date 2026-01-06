package com.fulfai.sellingpartner.email;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@IfBuildProfile("dev")
public class InviteEmailSenderDev implements InviteEmailSender {
    @ConfigProperty(name = "app.public-base-url")
    String publicBaseUrl;

    @Override
    public void sendJoinRequestApprovalEmail(
            String ownerUserId,
            String companyId,
            String requestId,
            String approvalToken
    ) {

        String approvalUrl =
                "/company/" + companyId + "/join-requests/approve-by-token?token=" + approvalToken;

        Log.infof(
            """
            =================== DEV EMAIL ===================
            Owner User ID : %s
            Company ID    : %s
            Request ID    : %s

            ✅ APPROVE JOIN REQUEST:
            %s
            =================================================
            """,
            ownerUserId,
            companyId,
            requestId,
            approvalUrl
        );
    }
}
