package com.fulfai.sellingpartner.email;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
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
            String ownerEmail,
            String companyName,
            String approvalUrl,
            String rejectUrl
    ) {
        /*
         * TODO (PROD):
         * - Build SES SendEmailRequest
         * - Subject: "Approve join request for " + companyName
         * - HTML body with buttons:
         *      Approve → approvalUrl
         *      Reject  → rejectUrl
         */

        Log.infof(
                """
                =================== SES EMAIL ===================
                To      : %s
                Company : %s

                APPROVE:
                %s

                REJECT:
                %s
                =================================================
                """,
                ownerEmail,
                companyName,
                approvalUrl,
                rejectUrl
        );

        // Placeholder — real SES send will be added later
        // sesClient.sendEmail(sendEmailRequest);
    }
}
