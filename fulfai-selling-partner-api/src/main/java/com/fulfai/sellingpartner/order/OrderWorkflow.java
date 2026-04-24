package com.fulfai.sellingpartner.order;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.ws.rs.core.Response;

@RegisterForReflection
public final class OrderWorkflow {

    private OrderWorkflow() {
    }

    public enum OrderAction {
        ACCEPT,
        REJECT,
        REQUEST_CHANGE,
        APPROVE_CHANGE,
        REJECT_CHANGE,
        REQUEST_CANCEL,
        DECIDE_CANCEL,
        MARK_PREPARING,
        MARK_READY,
        MARK_PICKED_UP,
        MARK_ON_THE_WAY,
        MARK_DELIVERED,
        MARK_FAILED,
        MARK_RETURNED,
        MARK_REFUNDED,
        CREATE_ISSUE,
        RESOLVE_ISSUE_REFUND,
        RESOLVE_ISSUE_REDELIVERY,
        RESOLVE_ISSUE_REPLACEMENT,
        REJECT_CLAIM
    }

    private static final Map<FulfillmentStatus, Set<FulfillmentStatus>> TRANSITIONS = new EnumMap<>(FulfillmentStatus.class);
    private static final Map<OrderAction, Set<OrderActorRole>> ACTION_ROLES = new EnumMap<>(OrderAction.class);

    static {
        add(FulfillmentStatus.CREATED, FulfillmentStatus.ACCEPTED, FulfillmentStatus.CANCELLED);
        add(FulfillmentStatus.ACCEPTED, FulfillmentStatus.PREPARING, FulfillmentStatus.CANCELLED);
        add(FulfillmentStatus.PREPARING, FulfillmentStatus.READY, FulfillmentStatus.CANCELLED);
        add(FulfillmentStatus.READY, FulfillmentStatus.PICKED_UP, FulfillmentStatus.CANCELLED);
        add(FulfillmentStatus.PICKED_UP, FulfillmentStatus.ON_THE_WAY, FulfillmentStatus.FAILED);
        add(FulfillmentStatus.ON_THE_WAY, FulfillmentStatus.DELIVERED, FulfillmentStatus.FAILED, FulfillmentStatus.RETURNED);
        add(FulfillmentStatus.DELIVERED, FulfillmentStatus.RETURNED, FulfillmentStatus.REFUNDED);
        add(FulfillmentStatus.FAILED, FulfillmentStatus.RETURNED, FulfillmentStatus.REFUNDED);
        add(FulfillmentStatus.RETURNED, FulfillmentStatus.REFUNDED);
        add(FulfillmentStatus.CANCELLED);
        add(FulfillmentStatus.REFUNDED);

        allow(OrderAction.ACCEPT, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.REJECT, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.REQUEST_CHANGE, OrderActorRole.CUSTOMER, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.APPROVE_CHANGE, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.REJECT_CHANGE, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.REQUEST_CANCEL, OrderActorRole.CUSTOMER, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.DECIDE_CANCEL, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.MARK_PREPARING, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.MARK_READY, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.MARK_PICKED_UP, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.MARK_ON_THE_WAY, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.MARK_DELIVERED, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.MARK_FAILED, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.MARK_RETURNED, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.MARK_REFUNDED, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.CREATE_ISSUE, OrderActorRole.CUSTOMER, OrderActorRole.VENDOR, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.RESOLVE_ISSUE_REFUND, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.RESOLVE_ISSUE_REDELIVERY, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.RESOLVE_ISSUE_REPLACEMENT, OrderActorRole.ADMIN, OrderActorRole.OPS);
        allow(OrderAction.REJECT_CLAIM, OrderActorRole.ADMIN, OrderActorRole.OPS);
    }

    private static void add(FulfillmentStatus from, FulfillmentStatus... to) {
        TRANSITIONS.put(from, to.length == 0 ? EnumSet.noneOf(FulfillmentStatus.class) : EnumSet.of(to[0], to));
    }

    private static void allow(OrderAction action, OrderActorRole... roles) {
        ACTION_ROLES.put(action, EnumSet.of(roles[0], roles));
    }

    public static void validateTransition(FulfillmentStatus from, FulfillmentStatus to) {
        Set<FulfillmentStatus> allowed = TRANSITIONS.getOrDefault(from, EnumSet.noneOf(FulfillmentStatus.class));
        if (!allowed.contains(to)) {
            throw new OrderWorkflowException(
                    Response.Status.CONFLICT,
                    "INVALID_STATUS_TRANSITION",
                    "Cannot transition order from " + from.value() + " to " + to.value());
        }
    }

    public static void validateActionPermission(OrderAction action, OrderActorRole actorRole) {
        Set<OrderActorRole> allowedRoles = ACTION_ROLES.getOrDefault(action, EnumSet.noneOf(OrderActorRole.class));
        if (!allowedRoles.contains(actorRole)) {
            throw new OrderWorkflowException(
                    Response.Status.FORBIDDEN,
                    "ACTION_FORBIDDEN",
                    "Action " + action.name().toLowerCase() + " is not allowed for actor role " + actorRole.name().toLowerCase());
        }
    }

    public static void validateActionAllowedForState(FulfillmentStatus current, OrderAction action) {
        if (!isActionPossible(current, action)) {
            throw new OrderWorkflowException(
                    Response.Status.CONFLICT,
                    "ACTION_NOT_ALLOWED_IN_STATE",
                    "Action " + action.name().toLowerCase() + " is not allowed when order status is " + current.value());
        }
    }

    public static Set<FulfillmentStatus> allowedNextStatuses(FulfillmentStatus from) {
        return TRANSITIONS.getOrDefault(from, EnumSet.noneOf(FulfillmentStatus.class));
    }

    public static Set<String> allowedActionNamesForActor(FulfillmentStatus current, OrderActorRole actorRole) {
        return ACTION_ROLES.entrySet()
                .stream()
                .filter(entry -> entry.getValue().contains(actorRole))
                .filter(entry -> isActionPossible(current, entry.getKey()))
                .map(entry -> entry.getKey().name().toLowerCase())
                .collect(Collectors.toSet());
    }

    private static boolean isActionPossible(FulfillmentStatus current, OrderAction action) {
        return switch (action) {
            case ACCEPT -> current == FulfillmentStatus.CREATED;
            case REJECT -> current == FulfillmentStatus.CREATED
                || current == FulfillmentStatus.ACCEPTED
                || current == FulfillmentStatus.PREPARING;
            case REQUEST_CHANGE -> current == FulfillmentStatus.ACCEPTED
                || current == FulfillmentStatus.PREPARING
                || current == FulfillmentStatus.READY;
            case APPROVE_CHANGE, REJECT_CHANGE -> current == FulfillmentStatus.ACCEPTED
                || current == FulfillmentStatus.PREPARING;
            case REQUEST_CANCEL, DECIDE_CANCEL -> current == FulfillmentStatus.CREATED
                || current == FulfillmentStatus.ACCEPTED
                || current == FulfillmentStatus.PREPARING
                || current == FulfillmentStatus.READY;
            case MARK_PREPARING -> current == FulfillmentStatus.ACCEPTED;
            case MARK_READY -> current == FulfillmentStatus.PREPARING;
            case MARK_PICKED_UP -> current == FulfillmentStatus.READY;
            case MARK_ON_THE_WAY -> current == FulfillmentStatus.PICKED_UP;
            case MARK_DELIVERED -> current == FulfillmentStatus.ON_THE_WAY;
            case MARK_FAILED -> current == FulfillmentStatus.PICKED_UP
                || current == FulfillmentStatus.ON_THE_WAY;
            case MARK_RETURNED -> current == FulfillmentStatus.ON_THE_WAY
                || current == FulfillmentStatus.DELIVERED
                || current == FulfillmentStatus.FAILED;
            case MARK_REFUNDED -> current == FulfillmentStatus.CANCELLED
                || current == FulfillmentStatus.FAILED
                || current == FulfillmentStatus.RETURNED
                || current == FulfillmentStatus.DELIVERED;
            case CREATE_ISSUE -> true;
            case RESOLVE_ISSUE_REFUND, RESOLVE_ISSUE_REDELIVERY, RESOLVE_ISSUE_REPLACEMENT, REJECT_CLAIM -> true;
        };
    }
}