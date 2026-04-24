package com.fulfai.sellingpartner.order;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class OrderWorkflowTest {

    @Test
    void shouldAllowValidTransition() {
        OrderWorkflow.validateTransition(
                                FulfillmentStatus.CREATED,
                FulfillmentStatus.ACCEPTED);
    }

    @Test
    void shouldRejectInvalidTransition() {
        assertThrows(
                OrderWorkflowException.class,
                () -> OrderWorkflow.validateTransition(
                        FulfillmentStatus.CREATED,
                        FulfillmentStatus.DELIVERED));
    }

    @Test
    void shouldEnforcePermissions() {
        assertThrows(
                OrderWorkflowException.class,
                () -> OrderWorkflow.validateActionPermission(
                        OrderWorkflow.OrderAction.REJECT,
                        OrderActorRole.CUSTOMER));

        OrderWorkflow.validateActionPermission(
                OrderWorkflow.OrderAction.REJECT,
                OrderActorRole.VENDOR);
    }

    @Test
    void shouldExposeActorSpecificNextActions() {
        Set<String> customerActions = OrderWorkflow.allowedActionNamesForActor(
                FulfillmentStatus.ACCEPTED,
                OrderActorRole.CUSTOMER);

        assertTrue(customerActions.contains("request_change"));
        assertTrue(customerActions.contains("request_cancel"));
        assertFalse(customerActions.contains("reject"));
    }

        @Test
        void shouldRejectActionNotAllowedForCurrentState() {
                assertThrows(
                                OrderWorkflowException.class,
                                () -> OrderWorkflow.validateActionAllowedForState(
                                                FulfillmentStatus.DELIVERED,
                                                OrderWorkflow.OrderAction.REQUEST_CHANGE));
        }

        @Test
        void shouldNotAllowCancelAfterPickedUp() {
                Set<String> customerActions = OrderWorkflow.allowedActionNamesForActor(
                                FulfillmentStatus.PICKED_UP,
                                OrderActorRole.CUSTOMER);

                assertFalse(customerActions.contains("request_cancel"));
        }
}
