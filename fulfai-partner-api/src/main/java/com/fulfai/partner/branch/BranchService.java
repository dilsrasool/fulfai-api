package com.fulfai.partner.branch;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class BranchService {

    @Inject
    BranchRepository branchRepository;

    @Inject
    BranchMapper branchMapper;

    public BranchResponseDTO createBranch(String companyId, @Valid BranchRequestDTO branchDTO) {
        Branch branch = branchMapper.toEntity(branchDTO);

        Instant now = Instant.now();
        branch.setCompanyId(companyId);
        branch.setBranchId(UUID.randomUUID().toString());
        branch.setCreatedAt(now);
        branch.setUpdatedAt(now);

        if (branch.getIsActive() == null) {
            branch.setIsActive(true);
        }

        branchRepository.save(branch);
        Log.debugf("Created branch with id: %s for company: %s", branch.getBranchId(), companyId);

        return branchMapper.toResponseDTO(branch);
    }

    public BranchResponseDTO getBranchById(String companyId, String branchId) {
        Log.debugf("Getting branch by companyId: %s, branchId: %s", companyId, branchId);
        Branch branch = branchRepository.getById(companyId, branchId);
        if (branch != null) {
            return branchMapper.toResponseDTO(branch);
        } else {
            throw new NotFoundException("Branch not found with id: " + branchId);
        }
    }

    public PaginatedResponse<BranchResponseDTO> getBranchesByCompanyId(String companyId, String nextToken, Integer limit) {
        Log.debugf("Getting branches for company: %s", companyId);
        PaginatedResponse<Branch> response = branchRepository.getByCompanyId(companyId, nextToken, limit);

        return PaginatedResponse.<BranchResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(branchMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public BranchResponseDTO updateBranch(String companyId, String branchId, @Valid BranchRequestDTO branchDTO) {
        Branch originalBranch = branchRepository.getById(companyId, branchId);
        if (originalBranch != null) {
            Branch branch = branchMapper.toEntity(branchDTO);
            branch.setCompanyId(companyId);
            branch.setBranchId(branchId);
            branch.setCreatedAt(originalBranch.getCreatedAt());
            branch.setUpdatedAt(Instant.now());

            branchRepository.save(branch);
            Log.debugf("Updated branch with id: %s", branchId);

            return branchMapper.toResponseDTO(branch);
        } else {
            throw new NotFoundException("Branch not found with id: " + branchId);
        }
    }

    public void deleteBranch(String companyId, String branchId) {
        Branch branch = branchRepository.getById(companyId, branchId);
        if (branch != null) {
            branchRepository.delete(companyId, branchId);
            Log.debugf("Deleted branch with id: %s", branchId);
        } else {
            throw new NotFoundException("Branch not found with id: " + branchId);
        }
    }
}
