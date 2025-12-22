package com.fulfai.sellingpartner.company;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import io.quarkus.security.identity.SecurityIdentity;

import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRole;
import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRoleRepository;
import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRoleResponseDTO;

@ApplicationScoped
public class CompanyService {

    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyMapper companyMapper;

    @Inject
    UserCompanyRoleRepository userCompanyRoleRepository;

    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Create a new company and automatically set ownerSub from the logged-in user. also add a record in UserCompanyRole table
     */
    public CompanyResponseDTO createCompany(@Valid CompanyRequestDTO companyDTO) {
            Company company = companyMapper.toEntity(companyDTO);

            Instant now = Instant.now();
            company.setCreatedAt(now);
            company.setUpdatedAt(now);
            company.setId(UUID.randomUUID().toString());

            // Set ownerSub from authenticated user
            String sub = securityIdentity.getPrincipal().getName();
            company.setOwnerSub(sub);

            // Save company
            companyRepository.save(company);
            Log.debugf("Created company with id: %s, ownerSub: %s", company.getId(), sub);

            // ✅ Also create corresponding UserCompanyRole record
            UserCompanyRole role = new UserCompanyRole();
            role.setUserId(sub);
            role.setCompanyId(company.getId());
            role.setRole("OWNER"); // or whatever default role you want
            userCompanyRoleRepository.save(role);

            return enrichWithUsers(companyMapper.toResponseDTO(company));
}


    /**
     * Get company by primary key id
     */
    public CompanyResponseDTO getCompanyById(String id) {
        Company company = companyRepository.getById(id);
        if (company != null) {
            return enrichWithUsers(companyMapper.toResponseDTO(company));
        } else {
            throw new NotFoundException("Company not found with id: " + id);
        }
    }

    /**
     * Get first company for currently logged-in user (/company/me)
     */
    public CompanyResponseDTO getCompanyForCurrentUser() {
        String sub = securityIdentity.getPrincipal().getName();
        Company company = companyRepository.getByOwnerSub(sub);
        if (company != null) {
            return enrichWithUsers(companyMapper.toResponseDTO(company));
        } else {
            throw new NotFoundException("Company not found for current user");
        }
    }

    /**
     * Get ALL companies for currently logged-in user (/company/me/all)
     */
    public List<CompanyResponseDTO> getAllCompaniesForCurrentUser() {
        String sub = securityIdentity.getPrincipal().getName();
        List<Company> companies = companyRepository.getAllByOwnerSub(sub);
        return companies.stream()
                .map(companyMapper::toResponseDTO)
                .map(this::enrichWithUsers)
                .collect(Collectors.toList());
    }

    /**
     * Update company by id
     */
    public CompanyResponseDTO updateCompanyById(String id, @Valid CompanyRequestDTO companyDTO) {
        Company originalCompany = companyRepository.getById(id);
        if (originalCompany != null) {
            Company company = companyMapper.toEntity(companyDTO);
            company.setId(id);
            company.setOwnerSub(originalCompany.getOwnerSub()); // preserve ownerSub
            company.setCreatedAt(originalCompany.getCreatedAt());
            company.setUpdatedAt(Instant.now()); // update timestamp

            companyRepository.save(company);
            Log.debugf("Updated company with id: %s", id);

            return enrichWithUsers(companyMapper.toResponseDTO(company));
        } else {
            throw new NotFoundException("Company not found with id: " + id);
        }
    }

    /**
     * Delete company by id
     */
    public void deleteCompanyById(String id) {
        Company company = companyRepository.getById(id);
        if (company != null) {
            companyRepository.delete(id);
            Log.debugf("Deleted company with id: %s", id);
        } else {
            throw new NotFoundException("Company not found with id: " + id);
        }
    }

    /**
     * Helper: enrich CompanyResponseDTO with users from UserCompanyRoleRepository
     */
    private CompanyResponseDTO enrichWithUsers(CompanyResponseDTO dto) {
        List<UserCompanyRole> roles = userCompanyRoleRepository.getByCompanyId(dto.getId());
        List<UserCompanyRoleResponseDTO> roleDTOs = roles.stream()
                .map(r -> new UserCompanyRoleResponseDTO(r.getUserId(), r.getCompanyId(), r.getRole()))
                .collect(Collectors.toList());
        dto.setUsers(roleDTOs);
        return dto;
    }
}
