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

    // ✅ Company GUID (primary identifier)
    String companyId = UUID.randomUUID().toString();

    company.setId(companyId);
    company.setJoinCode(companyId); // 🔑 JOIN CODE = COMPANY GUID
    company.setCreatedAt(now);
    company.setUpdatedAt(now);

    String ownerSub = securityIdentity.getPrincipal().getName();
    company.setOwnerSub(ownerSub);

    // ✅ Explicitly preserve mapped optional fields (safe)
    company.setPhoneNumber(company.getPhoneNumber());
    company.setWebsite(company.getWebsite());
    company.setState(company.getState());

    companyRepository.save(company);

    Log.infof(
        "Created company %s (joinCode=%s) by user %s",
        companyId,
        company.getJoinCode(),
        ownerSub
    );

    // ✅ Assign OWNER role
    UserCompanyRole role = new UserCompanyRole();
    role.setUserId(ownerSub);
    role.setCompanyAndBranch(companyId, null);
    role.setRole("OWNER");

    userCompanyRoleRepository.save(role);

    return enrichWithUsers(toResponse(company));
}


    /* ============================
       GET COMPANY
    ============================ */

    public CompanyResponseDTO getCompanyById(String id) {

        Company company = companyRepository.getById(id);
        if (company == null) {
            throw new NotFoundException("Company not found with id: " + id);
        }

        return enrichWithUsers(toResponse(company));
    }

    /**
     * Returns null if user owns no company
     */
    public CompanyResponseDTO getCompanyForCurrentUser() {

        String sub = securityIdentity.getPrincipal().getName();
        Company company = companyRepository.getByOwnerSub(sub);

        if (company == null) {
            return null;
        }

        return enrichWithUsers(toResponse(company));
    }

    public List<CompanyResponseDTO> getAllCompaniesForCurrentUser() {

        String sub = securityIdentity.getPrincipal().getName();

        return companyRepository.getAllByOwnerSub(sub)
                .stream()
                .map(this::toResponse)
                .map(this::enrichWithUsers)
                .collect(Collectors.toList());
    }

    /* ============================
       UPDATE COMPANY
    ============================ */

    public CompanyResponseDTO updateCompanyById(
        String id,
        @Valid CompanyRequestDTO dto
) {
    Company existing = companyRepository.getById(id);
    if (existing == null) {
        throw new NotFoundException("Company not found with id: " + id);
    }

    existing.setName(dto.getName());
    existing.setAddress(dto.getAddress());
    existing.setCity(dto.getCity());
    existing.setState(dto.getState());
    existing.setCountry(dto.getCountry());
    existing.setEmail(dto.getEmail());
    existing.setPhoneNumber(dto.getPhoneNumber());
    existing.setLicenseNo(dto.getLicenseNo());
    existing.setTrn(dto.getTrn());
    existing.setWebsite(dto.getWebsite());
    existing.setLogo(dto.getLogo());
    existing.setDescription(dto.getDescription());
    existing.setOperatingCountries(dto.getOperatingCountries());

    existing.setUpdatedAt(Instant.now());

    companyRepository.save(existing);

    return enrichWithUsers(toResponse(existing));
}


    /* ============================
       DELETE COMPANY
    ============================ */

    public void deleteCompanyById(String id) {

        Company company = companyRepository.getById(id);
        if (company == null) {
            throw new NotFoundException("Company not found with id: " + id);
        }

        // Cleanup roles
        userCompanyRoleRepository.getByCompanyId(id)
                .forEach(role ->
                        userCompanyRoleRepository.delete(
                                role.getUserId(),
                                role.getCompanyId(),
                                role.getBranchId()
                        )
                );

        companyRepository.delete(id);
        Log.warnf("Deleted company %s and cleaned up roles", id);
    }

    /* ============================
       DTO HELPERS
    ============================ */

    private CompanyResponseDTO toResponse(Company company) {
        CompanyResponseDTO dto = companyMapper.toResponseDTO(company);

        // ✅ Explicit frontend aliases
        dto.setCompanyGuid(company.getId());
        dto.setJoinCode(company.getJoinCode());

        return dto;
    }

    private CompanyResponseDTO enrichWithUsers(CompanyResponseDTO dto) {

        List<UserCompanyRoleResponseDTO> users =
                userCompanyRoleRepository.getByCompanyId(dto.getId())
                        .stream()
                        .map(role -> {
                            UserCompanyRoleResponseDTO r =
                                    new UserCompanyRoleResponseDTO();
                            r.setUserId(role.getUserId());
                            r.setCompanyId(role.getCompanyId());
                            r.setBranchId(role.getBranchId());
                            r.setRole(role.getRole());
                            r.setDisplayName(resolveDisplayName(role.getUserId()));
                            return r;
                        })
                        .collect(Collectors.toList());

        dto.setUsers(users);
        return dto;
    }

    /* ============================
       DISPLAY NAME (SAFE)
    ============================ */

    private String resolveDisplayName(String userId) {
        if (userId == null) return "Unknown";
        if (userId.contains("@")) return userId;
        return "User-" + userId.substring(0, Math.min(6, userId.length()));
    }
}
