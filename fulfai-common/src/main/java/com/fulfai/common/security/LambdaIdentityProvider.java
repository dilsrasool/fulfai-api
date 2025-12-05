package com.fulfai.common.security;

import java.util.Optional;

import io.quarkus.amazon.lambda.http.LambdaAuthenticationRequest;
import io.quarkus.amazon.lambda.http.model.AwsProxyRequest;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface LambdaIdentityProvider extends IdentityProvider<LambdaAuthenticationRequest> {

    @Override
    default public Class<LambdaAuthenticationRequest> getRequestType() {
        return LambdaAuthenticationRequest.class;
    }

    @Override
    default public Uni<SecurityIdentity> authenticate(LambdaAuthenticationRequest request,
            AuthenticationRequestContext context) {
        AwsProxyRequest proxyRequest = request.getEvent();

        SecurityIdentity identity = authenticate(proxyRequest, context);
        if (identity == null) {
            return Uni.createFrom().optional(Optional.empty());
        }
        return Uni.createFrom().item(identity);
    }

    /**
     * You must override this method unless you directly override
     * IdentityProvider.authenticate
     *
     * @param event
     * @param context
     * @return SecurityIdentity
     */
    default SecurityIdentity authenticate(AwsProxyRequest event, AuthenticationRequestContext context) {
        throw new IllegalStateException("You must override this method");
    }
}
