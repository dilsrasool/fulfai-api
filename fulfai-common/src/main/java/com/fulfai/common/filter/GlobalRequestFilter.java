package com.fulfai.common.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.quarkus.logging.Log;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Log.debugf("REQUEST: %s %s", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri());

        InputStream originalStream = requestContext.getEntityStream();
        if (originalStream != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = originalStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }

                String bodyContent = baos.toString("UTF-8");
                if (!bodyContent.isEmpty()) {
                    Log.debugf("REQUEST_BODY: %s", bodyContent);
                }

                ByteArrayInputStream newStream = new ByteArrayInputStream(baos.toByteArray());
                requestContext.setEntityStream(newStream);

            } catch (IOException e) {
                Log.error("Error reading request body: " + e.getMessage());
            }
        }
    }
}
