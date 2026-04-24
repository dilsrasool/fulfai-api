package com.fulfai.sellingpartner.order;

import java.time.Instant;
import java.util.Map;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@DynamoDbBean
@RegisterForReflection
public class OrderTimelineEvent {

    private String eventId;
    private String action;
    private String actorId;
    private String actorRole;
    private String fromStatus;
    private String toStatus;
    private String reasonCode;
    private String note;
    private String idempotencyKey;
    private Instant timestamp;
    private Map<String, String> metadata;

    @DynamoDbAttribute("eventId")
    public String getEventId() {
        return eventId;
    }

    @DynamoDbAttribute("action")
    public String getAction() {
        return action;
    }

    @DynamoDbAttribute("actorId")
    public String getActorId() {
        return actorId;
    }

    @DynamoDbAttribute("actorRole")
    public String getActorRole() {
        return actorRole;
    }

    @DynamoDbAttribute("fromStatus")
    public String getFromStatus() {
        return fromStatus;
    }

    @DynamoDbAttribute("toStatus")
    public String getToStatus() {
        return toStatus;
    }

    @DynamoDbAttribute("reasonCode")
    public String getReasonCode() {
        return reasonCode;
    }

    @DynamoDbAttribute("note")
    public String getNote() {
        return note;
    }

    @DynamoDbAttribute("idempotencyKey")
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    @DynamoDbAttribute("timestamp")
    public Instant getTimestamp() {
        return timestamp;
    }

    @DynamoDbAttribute("metadata")
    public Map<String, String> getMetadata() {
        return metadata;
    }
}