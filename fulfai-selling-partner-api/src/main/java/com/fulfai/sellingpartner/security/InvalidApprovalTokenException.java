package com.fulfai.sellingpartner.security;

public class InvalidApprovalTokenException
        extends RuntimeException {

    public InvalidApprovalTokenException(String message) {
        super(message);
    }

    public InvalidApprovalTokenException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
