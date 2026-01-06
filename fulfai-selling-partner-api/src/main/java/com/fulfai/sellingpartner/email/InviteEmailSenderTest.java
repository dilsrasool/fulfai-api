package com.fulfai.sellingpartner.email;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IfBuildProfile("dev|test")
public class InviteEmailSenderTest implements InviteEmailSender {

    @Override
    public void sendJoinRequestApprovalEmail(
            String ownerEmail,
            String companyName,
            String approvalUrl,
            String rejectUrl
    ) {
        Log.infof(
                """
                ================= DEV / TEST EMAIL =================
                To      : %s
                Company : %s

                APPROVE:
                %s

                REJECT:
                %s
                ====================================================
                """,
                ownerEmail,
                companyName,
                approvalUrl,
                rejectUrl
        );
    }
}
