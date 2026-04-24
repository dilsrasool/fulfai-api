package com.fulfai.sellingpartner.order;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class OrderWorkflowExceptionMapper implements ExceptionMapper<OrderWorkflowException> {

    @Override
    public Response toResponse(OrderWorkflowException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", exception.getResponse().getStatus());
        body.put("errorCode", exception.getCode());
        body.put("message", exception.getMessage());
        body.put("timestamp", System.currentTimeMillis());

        return Response.status(exception.getResponse().getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
