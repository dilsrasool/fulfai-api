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
           ONLY THESE ENDPOINTS ARE PUBLIC
        ====================================================== */

        if (path != null &&
                (
                        path.startsWith("/api/selling-partner/public/products")
                        || path.startsWith("/api/selling-partner/public/companies")
                        || path.startsWith("/api/selling-partner/public/categories")
                        || path.startsWith("/api/selling-partner/public/branches")
                        || path.startsWith("/health")
                )
        ) {

            Log.debugf(
                    "SECURITY_AUTH: Public browsing endpoint → %s",
                    path
            );

            return QuarkusSecurityIdentity.builder()
                    .setPrincipal(new QuarkusPrincipal("ANONYMOUS"))
                    .addAttribute("auth_type", "PUBLIC")
                    .build();
        }



        /* =====================================================
           AUTHENTICATED FLOW — REQUIRED FOR ORDERS
        ====================================================== */

        if (event == null
                || event.getRequestContext() == null
                || event.getRequestContext().getIdentity() == null) {

            Log.debug("SECURITY_AUTH: No request identity");

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


        String sub =
                CognitoUtils.extractSubFromString(cognitoIdentity);


        Log.debugf(
                "SECURITY_AUTH: sub=%s authType=%s",
                sub,
                authType
        );


        if (sub == null || sub.isBlank()) {

            Log.debug("SECURITY_AUTH: Invalid Cognito token");

            return null;
        }



        /* =====================================================
           SUCCESS AUTHENTICATION
        ====================================================== */

        Principal principal =
                new QuarkusPrincipal(sub);


        return QuarkusSecurityIdentity.builder()

                .setPrincipal(principal)

                .addAttribute("auth_type", authType)

                .addAttribute("sub", sub)

                .addAttribute("cognito_sub", sub)

                .addRole("customer")   // ⭐ IMPORTANT

                .build();

    }

}
