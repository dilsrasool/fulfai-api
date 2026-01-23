package com.fulfai.common.security;

import java.security.Principal;

import io.quarkus.amazon.lambda.http.model.AwsProxyRequest;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CognitoSecurityProvider implements LambdaIdentityProvider {

    @Override
    public SecurityIdentity authenticate(
            AwsProxyRequest event,
            AuthenticationRequestContext context
    ) {

        String path = event != null ? event.getPath() : null;

        Log.debugf("SECURITY_AUTH: Incoming path=%s", path);

        /* =====================================================
           🔓 PUBLIC ENDPOINTS (NO AUTH)
        ====================================================== */

        // ✅ PUBLIC: Branch Active (browse portal without login)
        if (path != null && path.equals("/api/selling-partner/branch/active")) {
            Log.debugf("SECURITY_AUTH: Public endpoint → %s (anonymous identity)", path);

            return QuarkusSecurityIdentity.builder()
                    .setPrincipal(new QuarkusPrincipal("ANONYMOUS"))
                    .addAttribute("auth_type", "PUBLIC")
                    .build();
        }

        // 🔓 PUBLIC EMAIL TOKEN ENDPOINTS (NO AUTH)
        if (path != null &&
            (path.contains("/join-requests/approve-by-token")
          || path.contains("/join-requests/reject-by-token"))) {

            Log.debugf(
                "SECURITY_AUTH: Public token endpoint → %s (anonymous identity)",
                path
            );

            return QuarkusSecurityIdentity.builder()
                    .setPrincipal(new QuarkusPrincipal("ANONYMOUS"))
                    .addAttribute("auth_type", "PUBLIC_TOKEN")
                    .build();
        }

        /* =====================================================
           🔐 NORMAL AUTHENTICATED FLOW
        ====================================================== */

        if (event.getRequestContext() == null
            || event.getRequestContext().getIdentity() == null) {

            Log.debug("SECURITY_AUTH: No request identity found");
            return null;
        }

        String cognitoIdentity =
                event.getRequestContext()
                     .getIdentity()
                     .getCognitoAuthenticationProvider();

        String authType =
                event.getRequestContext()
                     .getIdentity()
                     .getCognitoAuthenticationType();

        String sub = CognitoUtils.extractSubFromString(cognitoIdentity);

        Log.debugf(
            "SECURITY_AUTH: sub=%s, authType=%s",
            sub,
            authType
        );

        if (sub == null || sub.isBlank()) {
            Log.debug("SECURITY_AUTH: No Cognito sub found, returning null");
            return null;
        }

        Principal principal = new QuarkusPrincipal(sub);

        return QuarkusSecurityIdentity.builder()
                .setPrincipal(principal)

                // legacy compatibility
                .addAttribute("auth_type", authType)

                // ✅ REQUIRED BY CompanyService
                .addAttribute("sub", sub)

                // optional (kept for debugging)
                .addAttribute("cognito_sub", sub)

                .build();
    }
}
