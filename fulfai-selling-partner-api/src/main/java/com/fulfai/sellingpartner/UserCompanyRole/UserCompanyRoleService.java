package com.fulfai.sellingpartner.UserCompanyRole;

import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import com.fulfai.sellingpartner.company.Company;
import com.fulfai.sellingpartner.company.CompanyRepository;

@ApplicationScoped
public class UserCompanyRoleService {

    @Inject
    UserCompanyRoleRepository userCompanyRoleRepository;

    @Inject
    CompanyRepository companyRepository;

    /**
     * Add a user with a role to a company
     */
    public void addUserToCompany(UserCompanyRoleRequestDTO request) {
        Company company = companyRepository.getById(request.getCompanyId());
        if (company == null) {
            throw new NotFoundException("Company not found with id: " + request.getCompanyId());
        }

        UserCompanyRole userRole = new UserCompanyRole();
        userRole.setUserId(request.getUserId());
        userRole.setCompanyId(request.getCompanyId());
        userRole.setRole(request.getRole());

        userCompanyRoleRepository.save(userRole);
        Log.debugf("Added user %s with role %s to company %s",
                request.getUserId(), request.getRole(), request.getCompanyId());
    }

    /**
     * Get all users for a company
     */
    public List<UserCompanyRoleResponseDTO> getUsersForCompany(String companyId) {
        Company company = companyRepository.getById(companyId);
        if (company == null) {
            throw new NotFoundException("Company not found with id: " + companyId);
        }

        return userCompanyRoleRepository.getAllByUserId(companyId).stream()
                .map(userRole -> {
                    UserCompanyRoleResponseDTO dto = new UserCompanyRoleResponseDTO();
                    dto.setUserId(userRole.getUserId());
                    dto.setCompanyId(userRole.getCompanyId());
                    dto.setRole(userRole.getRole());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Remove a user from a company
     */
    public void removeUserFromCompany(String companyId, String userId) {
        Company company = companyRepository.getById(companyId);
        if (company == null) {
            throw new NotFoundException("Company not found with id: " + companyId);
        }

        userCompanyRoleRepository.delete(userId, companyId);
        Log.debugf("Removed user %s from company %s", userId, companyId);
    }
}
