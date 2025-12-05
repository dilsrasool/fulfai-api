package com.fulfai.common.exception;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.logging.Log;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        Map<String, Object> response = new HashMap<>();

        int statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String message = exception.getMessage();

        Log.debug("Caught Exception: " + exception.getMessage() + " " + exception.getClass().getName());

        if (exception instanceof ClientErrorException) {
            ClientErrorException clientException = (ClientErrorException) exception;
            statusCode = clientException.getResponse().getStatus();
            Log.trace(exception.getMessage(), exception);
        } else if (exception instanceof WebApplicationException) {
            WebApplicationException webException = (WebApplicationException) exception;
            statusCode = webException.getResponse().getStatus();
            message = webException.getMessage();
        } else if (exception instanceof ProcessingException && exception.getCause() instanceof JsonbException) {
            statusCode = Response.Status.BAD_REQUEST.getStatusCode();
            message = exception.getCause().getMessage();
            Log.error(exception.getMessage(), exception);
        } else {
            message = "An unexpected error occurred";
            Log.error(exception.getMessage(), exception);
        }

        response.put("status", statusCode);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());

        return Response.status(statusCode)
                .entity(response)
                .type("application/json")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH")
                .header("Access-Control-Allow-Headers",
                    "Content-Type, Authorization, X-Requested-With, Accept, Origin, " +
                    "Access-Control-Request-Method, Access-Control-Request-Headers, " +
                    "X-Amz-Date, X-Api-Key, X-Amz-Security-Token, X-Amz-User-Agent")
                .header("Access-Control-Max-Age", "3600")
                .build();
    }
}
