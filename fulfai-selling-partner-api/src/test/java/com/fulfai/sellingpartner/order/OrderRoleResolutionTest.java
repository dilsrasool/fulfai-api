package com.fulfai.sellingpartner.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRole;
import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRoleRepository;
import com.fulfai.sellingpartner.account.AccountService;
import com.fulfai.sellingpartner.product.ProductRepository;

@ExtendWith(MockitoExtension.class)
class OrderRoleResolutionTest {

    @Mock
    AccountService accountService;

    @Mock
    OrderRepository orderRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    UserCompanyRoleRepository userCompanyRoleRepository;

    @Mock
    OrderMapper orderMapper;

    @InjectMocks
    OrderService orderService;

    @Test
    void shouldAllowBranchAssignedUserWhenBranchNotProvided() {
        String actorId = "u1";
        String companyId = "c1";

        UserCompanyRole branchRole = new UserCompanyRole();
        branchRole.setCompanyAndBranch(companyId, "b1");
        branchRole.setUserId(actorId);
        branchRole.setRole("STAFF");

        when(userCompanyRoleRepository.getRole(actorId, companyId, null)).thenReturn(null);
        when(userCompanyRoleRepository.hasAnyRoleInCompany(actorId, companyId)).thenReturn(true);

        OrderActorRole resolved = orderService.resolveSellerActorRole(actorId, companyId, null);

        assertEquals(OrderActorRole.VENDOR, resolved);
    }

    @Test
    void shouldRejectUserWithoutCompanyRole() {
        String actorId = "u2";
        String companyId = "c1";

        when(userCompanyRoleRepository.getRole(actorId, companyId, null)).thenReturn(null);
        when(userCompanyRoleRepository.hasAnyRoleInCompany(actorId, companyId)).thenReturn(false);

        assertThrows(
                OrderWorkflowException.class,
                () -> orderService.resolveSellerActorRole(actorId, companyId, null));
    }
}
