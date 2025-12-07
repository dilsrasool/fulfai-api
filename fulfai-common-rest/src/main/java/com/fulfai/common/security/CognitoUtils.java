package com.fulfai.common.security;

/**
 * Utility methods for Cognito authentication
 */
public class CognitoUtils {

    /**
     * Extract the user sub from Cognito authentication provider string.
     * The format is typically: "cognito-idp.region.amazonaws.com/poolId,cognito-idp.region.amazonaws.com/poolId:CognitoSignIn:sub"
     *
     * @param cognitoAuthenticationProvider the raw authentication provider string
     * @return the user sub or null if not found
     */
    public static String extractSubFromString(String cognitoAuthenticationProvider) {
        if (cognitoAuthenticationProvider == null) {
            return null;
        }
        int index = cognitoAuthenticationProvider.indexOf("CognitoSignIn:");
        if (index == -1) {
            return null;
        }
        return cognitoAuthenticationProvider.substring(index + "CognitoSignIn:".length());
    }
}
