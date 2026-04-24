package com.fulfai.sellingpartner.order;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum OrderActorRole {
    CUSTOMER,
    VENDOR,
    ADMIN,
    OPS
}