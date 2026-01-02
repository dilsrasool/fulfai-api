package com.fulfai.common.aws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;


@ApplicationScoped
public class SesClientProducer {

    @Produces
    public SesClient sesClient() {
        return SesClient.builder().build();
    }
}
