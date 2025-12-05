package com.fulfai.deliverypartner.company;

import java.time.Instant;
import java.util.UUID;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class CompanyService {

    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyMapper companyMapper;

    public CompanyResponseDTO createCompany(@Valid CompanyRequestDTO companyDTO) {
        Company company = companyMapper.toEntity(companyDTO);

        Instant now = Instant.now();
        company.setId(UUID.randomUUID().toString());
        company.setCreatedAt(now);
        company.setUpdatedAt(now);

        if (company.getIsActive() == null) {
            company.setIsActive(true);
        }

        companyRepository.save(company);
        Log.debugf("Created delivery company with id: %s", company.getId());

        return companyMapper.toResponseDTO(company);
    }

    public CompanyResponseDTO getCompanyById(String id) {
        Company company = companyRepository.getById(id);
        if (company != null) {
            return companyMapper.toResponseDTO(company);
        }
        throw new NotFoundException("Company not found with id: " + id);
    }

    public CompanyResponseDTO updateCompany(String id, @Valid CompanyRequestDTO companyDTO) {
        Company existingCompany = companyRepository.getById(id);
        if (existingCompany == null) {
            throw new NotFoundException("Company not found with id: " + id);
        }

        Company company = companyMapper.toEntity(companyDTO);
        company.setId(id);
        company.setCreatedAt(existingCompany.getCreatedAt());
        company.setUpdatedAt(Instant.now());

        companyRepository.save(company);
        Log.debugf("Updated delivery company with id: %s", id);

        return companyMapper.toResponseDTO(company);
    }

    public void deleteCompany(String id) {
        Company company = companyRepository.getById(id);
        if (company == null) {
            throw new NotFoundException("Company not found with id: " + id);
        }

        companyRepository.delete(id);
        Log.debugf("Deleted delivery company with id: %s", id);
    }
}
