package com.fulfai.partner.company;

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
        company.setCreatedAt(now);
        company.setUpdatedAt(now);
        company.setId(UUID.randomUUID().toString());

        companyRepository.save(company);
        Log.debugf("Created company with id: %s", company.getId());

        return companyMapper.toResponseDTO(company);
    }

    public CompanyResponseDTO getCompanyById(String id) {
        Log.debugf("Getting company by id: %s", id);
        Company company = companyRepository.getById(id);
        if (company != null) {
            return companyMapper.toResponseDTO(company);
        } else {
            throw new NotFoundException("Company not found with id: " + id);
        }
    }

    public CompanyResponseDTO updateCompanyById(String id, @Valid CompanyRequestDTO companyDTO) {
        Company originalCompany = companyRepository.getById(id);
        if (originalCompany != null) {
            Company company = companyMapper.toEntity(companyDTO);
            company.setId(id);
            company.setCreatedAt(originalCompany.getCreatedAt());
            company.setUpdatedAt(Instant.now());

            companyRepository.save(company);
            Log.debugf("Updated company with id: %s", id);

            return companyMapper.toResponseDTO(company);
        } else {
            throw new NotFoundException("Company not found with id: " + id);
        }
    }

    public void deleteCompanyById(String id) {
        Company company = companyRepository.getById(id);
        if (company != null) {
            companyRepository.delete(id);
            Log.debugf("Deleted company with id: %s", id);
        } else {
            throw new NotFoundException("Company not found with id: " + id);
        }
    }
}
