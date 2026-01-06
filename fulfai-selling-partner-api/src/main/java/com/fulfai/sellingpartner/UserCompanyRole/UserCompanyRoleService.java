package com.fulfai.sellingpartner.UserCompanyRole;

import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import com.fulfai.sellingpartner.company.Company;
import com.fulfai.sellingpartner.company.CompanyRepository;
import com.fulfai.sellingpartner.security.CognitoUserResolver;

@ApplicationScoped
public class UserCompanyRoleService {

    @Inject
    UserCompanyRoleRepository userCompanyRoleRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UserCompanyRoleMapper mapper;

    @Inject
    CognitoUserResolver cognitoUserResolver;

    /* ============================
       ADD USER (OWNER / ADMIN)
    ============================= */

    public void addUserToCompany(UserCompanyRoleRequestDTO request) {

        Company company = companyRepository.getById(request.getCompanyId());
        if (company == null) {
            throw new NotFoundException(
                "Company not found with id: " + request.getCompanyId()
            );
        }

        // Resolve email → Cognito sub
        String userSub = cognitoUserResolver.getSubByEmail(request.getEmail());
        if (userSub == null) {
            throw new BadRequestException(
                "User not found for email: " + request.getEmail()
            );
        }

        // Prevent duplicate role
        boolean exists = userCompanyRoleRepository.exists(
            userSub,
            request.getCompanyId(),
            request.getBranchId()
        );

        if (exists) {
            throw new BadRequestException(
                "User already exists in company/branch"
            );
        }

        UserCompanyRole role = new UserCompanyRole();
        role.setUserId(userSub);
        role.setCompanyAndBranch(
            request.getCompanyId(),
            request.getBranchId() // null = company-level
        );
        role.setRole(request.getRole());

        userCompanyRoleRepository.save(role);

        Log.debugf(
            "Added user %s (%s) to company %s (branch=%s)",
            request.getEmail(),
            userSub,
            request.getCompanyId(),
            request.getBranchId()
        );
    }

    /* ============================
       GET USERS (COMPANY)
    ============================= */

    public List<UserCompanyRoleResponseDTO> getUsersForCompany(String companyId) {

        Company company = companyRepository.getById(companyId);
        if (company == null) {
            throw new NotFoundException(
                "Company not found with id: " + companyId
            );
        }

        return userCompanyRoleRepository
            .getByCompanyId(companyId)
            .stream()
            .map(this::toSafeResponse)
            .collect(Collectors.toList());
    }

    /* ============================
       GET USERS (BRANCH)
    ============================= */

    public List<UserCompanyRoleResponseDTO> getUsersForBranch(
        String companyId,
        String branchId
    ) {

        Company company = companyRepository.getById(companyId);
        if (company == null) {
            throw new NotFoundException(
                "Company not found with id: " + companyId
            );
        }

        return userCompanyRoleRepository
            .getByCompanyId(companyId)
            .stream()
            .filter(r -> branchId.equals(r.getBranchId()))
            .map(this::toSafeResponse)
            .collect(Collectors.toList());
    }

    /* ============================
       REMOVE USER (EMAIL-BASED)
    ============================= */

    public void removeUserFromCompanyByEmail(
        String companyId,
        String branchId,
        String email
    ) {

        Company company = companyRepository.getById(companyId);
        if (company == null) {
            throw new NotFoundException(
                "Company not found: " + companyId
            );
        }

        String userSub = cognitoUserResolver.getSubByEmail(email);
        if (userSub == null) {
            throw new BadRequestException(
                "User not found for email: " + email
            );
        }

        boolean exists = userCompanyRoleRepository.exists(
            userSub,
            companyId,
            branchId
        );

        if (!exists) {
            throw new NotFoundException(
                "User is not associated with this company/branch"
            );
        }

        userCompanyRoleRepository.delete(
            userSub,
            companyId,
            branchId
        );

        Log.debugf(
            "Removed user %s (%s) from company %s (branch=%s)",
            email,
            userSub,
            companyId,
            branchId
        );
    }

    /* ============================
       PRIVATE HELPERS
    ============================= */

    /**
     * Convert entity → safe response DTO
     * (never expose internal Cognito sub directly)
     */
    private UserCompanyRoleResponseDTO toSafeResponse(
        UserCompanyRole role
    ) {

        UserCompanyRoleResponseDTO dto =
            mapper.toResponseDTO(role);

        dto.setDisplayName(resolveDisplayName(role.getUserId()));
        return dto;
    }

    /**
     * Display name resolver
     * (email if available, masked Cognito sub otherwise)
     */
    private String resolveDisplayName(String userId) {

        if (userId == null || userId.isBlank()) {
            return "User";
        }

        // Dev / fallback case
        if (userId.contains("@")) {
            return userId;
        }

        // Mask Cognito sub
        return userId.length() > 6
                ? "User-" + userId.substring(0, 6)
                : "User";
    }
}
