package com.fulfai.sellingpartner.order;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class OrderWorkflowException extends WebApplicationException {

    private final String code;

    public OrderWorkflowException(Response.Status status, String code, String message) {
        super(message, status);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}