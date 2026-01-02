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

    /* ============================
       CREATE COMPANY
    ============================ */

    public CompanyResponseDTO createCompany(@Valid CompanyRequestDTO companyDTO) {

        Company company = companyMapper.toEntity(companyDTO);

        Instant now = Instant.now();
        company.setId(UUID.randomUUID().toString());
        company.setCreatedAt(now);
        company.setUpdatedAt(now);

        String sub = securityIdentity.getPrincipal().getName();
        company.setOwnerSub(sub);

        companyRepository.save(company);
        Log.debugf("Created company %s by user %s", company.getId(), sub);

        // ✅ Assign OWNER role at company level
        UserCompanyRole role = new UserCompanyRole();
        role.setUserId(sub);
        role.setCompanyAndBranch(company.getId(), null);
        role.setRole("OWNER");

        userCompanyRoleRepository.save(role);

        return enrichWithUsers(companyMapper.toResponseDTO(company));
    }

    /* ============================
       GET COMPANY
    ============================ */

    public CompanyResponseDTO getCompanyById(String id) {
        Company company = companyRepository.getById(id);
        if (company == null) {
            throw new NotFoundException("Company not found with id: " + id);
        }
        return enrichWithUsers(companyMapper.toResponseDTO(company));
    }

    public CompanyResponseDTO getCompanyForCurrentUser() {
        String sub = securityIdentity.getPrincipal().getName();
        Company company = companyRepository.getByOwnerSub(sub);
        if (company == null) {
            throw new NotFoundException("Company not found for current user");
        }
        return enrichWithUsers(companyMapper.toResponseDTO(company));
    }

    public List<CompanyResponseDTO> getAllCompaniesForCurrentUser() {
        String sub = securityIdentity.getPrincipal().getName();
        return companyRepository.getAllByOwnerSub(sub)
                .stream()
                .map(companyMapper::toResponseDTO)
                .map(this::enrichWithUsers)
                .collect(Collectors.toList());
    }

    /* ============================
       UPDATE COMPANY
    ============================ */

    public CompanyResponseDTO updateCompanyById(
            String id,
            @Valid CompanyRequestDTO companyDTO
    ) {

        Company existing = companyRepository.getById(id);
        if (existing == null) {
            throw new NotFoundException("Company not found with id: " + id);
        }

        Company updated = companyMapper.toEntity(companyDTO);
        updated.setId(id);
        updated.setOwnerSub(existing.getOwnerSub());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(Instant.now());

        companyRepository.save(updated);
        Log.debugf("Updated company %s", id);

        return enrichWithUsers(companyMapper.toResponseDTO(updated));
    }

    /* ============================
       DELETE COMPANY
    ============================ */

    public void deleteCompanyById(String id) {

        Company company = companyRepository.getById(id);
        if (company == null) {
            throw new NotFoundException("Company not found with id: " + id);
        }

        // ✅ Cleanup roles
        userCompanyRoleRepository.getByCompanyId(id)
                .forEach(role ->
                        userCompanyRoleRepository.delete(
                                role.getUserId(),
                                role.getCompanyId(),
                                role.getBranchId()
                        )
                );

        companyRepository.delete(id);
        Log.debugf("Deleted company %s and cleaned up roles", id);
    }

    /* ============================
       ENRICH USERS (SAFE DTO)
    ============================ */

    private CompanyResponseDTO enrichWithUsers(CompanyResponseDTO dto) {

        List<UserCompanyRoleResponseDTO> users =
                userCompanyRoleRepository.getByCompanyId(dto.getId())
                        .stream()
                        .map(role -> {
                            UserCompanyRoleResponseDTO r = new UserCompanyRoleResponseDTO();
                            r.setUserId(role.getUserId());
                            r.setCompanyId(role.getCompanyId());
                            r.setBranchId(role.getBranchId());
                            r.setRole(role.getRole());

                            // ✅ Safe display name (no raw IDs)
                            r.setDisplayName(resolveDisplayName(role.getUserId()));

                            return r;
                        })
                        .collect(Collectors.toList());

        dto.setUsers(users);
        return dto;
    }

    /* ============================
       DISPLAY NAME RESOLVER
    ============================ */

    private String resolveDisplayName(String userId) {
        if (userId == null) return "Unknown";
        if (userId.contains("@")) return userId;
        return "User-" + userId.substring(0, Math.min(6, userId.length()));
    }
}
