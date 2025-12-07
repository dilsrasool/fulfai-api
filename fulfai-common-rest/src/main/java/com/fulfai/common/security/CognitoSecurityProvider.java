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
    public SecurityIdentity authenticate(AwsProxyRequest event, AuthenticationRequestContext context) {
        String cognitoIdentity = event.getRequestContext().getIdentity().getCognitoAuthenticationProvider();
        String sub = CognitoUtils.extractSubFromString(cognitoIdentity);
        String authType = event.getRequestContext().getIdentity().getCognitoAuthenticationType();

        Log.debugf("SECURITY_AUTH: sub=%s, authType=%s", sub, authType);

        if (sub == null) {
            Log.debug("SECURITY_AUTH: No Cognito sub found, returning null");
            return null;
        }

        Principal principal = new QuarkusPrincipal(sub);

        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();
        builder.setPrincipal(principal);
        builder.addAttribute("auth_type", authType);
        builder.addAttribute("cognito_sub", sub);

        return builder.build();
    }
}
