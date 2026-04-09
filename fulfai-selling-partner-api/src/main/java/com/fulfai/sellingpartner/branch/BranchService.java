package com.fulfai.sellingpartner.branch;

import java.util.Comparator;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.location.GeoHashUtil;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import com.fulfai.sellingpartner.publicapi.dto.PublicBranchDTO;


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

        applyLocation(branch, branchDTO.getLatitude(), branchDTO.getLongitude(), now);

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

        // ✅ NEW METHOD: Get all ACTIVE branches for a company (no pagination)
    public List<BranchResponseDTO> getAllActiveBranchesByCompany(String companyId) {
        Log.debugf("Getting ACTIVE branches for company: %s", companyId);

        // Get all branches (paginate with a big limit OR your repository can have a dedicated method)
        PaginatedResponse<Branch> response = branchRepository.getByCompanyId(companyId, null, 1000);

        return response.getItems().stream()
                .filter(branch -> Boolean.TRUE.equals(branch.getIsActive()))
                .map(branchMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
//TODO: This endpoint scans the whole Branch table, so if branches grow a lot, you should add a GSI on isActive.
    public List<BranchResponseDTO> getAllActiveBranchesAcrossAllCompanies() {
        Log.debug("Getting ALL ACTIVE branches across ALL companies");

        return branchRepository.getAllActiveBranchesAcrossAllCompanies()
                .stream()
                .map(branchMapper::toResponseDTO)
                .toList();
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
            Instant now = Instant.now();
            branch.setCompanyId(companyId);
            branch.setBranchId(branchId);
            branch.setCreatedAt(originalBranch.getCreatedAt());
            branch.setUpdatedAt(now);

            if (branchDTO.getLatitude() == null && branchDTO.getLongitude() == null) {
                branch.setLatitude(originalBranch.getLatitude());
                branch.setLongitude(originalBranch.getLongitude());
                branch.setGeoHash5(originalBranch.getGeoHash5());
                branch.setGeoHash6(originalBranch.getGeoHash6());
                branch.setLocationUpdatedAt(originalBranch.getLocationUpdatedAt());
            } else {
                applyLocation(branch, branchDTO.getLatitude(), branchDTO.getLongitude(), now);
            }

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

    /* ============================
   PUBLIC BROWSING (NO AUTH)
============================ */

public List<PublicBranchDTO> getPublicBranches(String companyId) {

    if (companyId == null || companyId.isBlank()) {
        throw new jakarta.ws.rs.BadRequestException("companyId is required");
    }

    // reuse your existing method (already filters active)
    List<BranchResponseDTO> activeBranches = getAllActiveBranchesByCompany(companyId);

    return activeBranches.stream()
            .map(this::toPublicBranchDTO)
            .collect(Collectors.toList());
}

public List<PublicBranchDTO> getAllPublicActiveBranchesAcrossAllCompanies() {
    return getAllActiveBranchesAcrossAllCompanies().stream()
            .map(this::toPublicBranchDTO)
            .collect(Collectors.toList());
}

public List<PublicBranchDTO> getNearbyPublicBranches(
        String companyId,
        Double latitude,
        Double longitude,
        Double radiusKm,
        Integer limit
) {

    if (latitude == null || longitude == null) {
        throw new BadRequestException("latitude and longitude are required");
    }

    double safeRadiusKm = radiusKm == null || radiusKm <= 0 ? 10.0 : radiusKm;
    int safeLimit = limit == null || limit <= 0 ? 20 : Math.min(limit, 50);

        return getNearbyBranchCandidates(companyId, latitude, longitude, safeRadiusKm).stream()
            .map(branch -> {
            PublicBranchDTO dto = toPublicBranchDTO(branch);
            dto.distanceKm = GeoHashUtil.calculateDistance(
                latitude,
                longitude,
                branch.getLatitude(),
                branch.getLongitude()
            );
            return dto;
            })
            .sorted(Comparator.comparing(dto -> dto.distanceKm))
            .limit(safeLimit)
            .collect(Collectors.toList());
    }

    public List<BranchResponseDTO> getNearbyBranchCandidates(
        String companyId,
        Double latitude,
        Double longitude,
        Double radiusKm
    ) {

        if (latitude == null || longitude == null) {
        throw new BadRequestException("latitude and longitude are required");
        }

        double safeRadiusKm = radiusKm == null || radiusKm <= 0 ? 10.0 : radiusKm;

            String userGeoHash5 = GeoHashUtil.encode(latitude, longitude, 5);
            Set<String> geoCandidates = new HashSet<>(GeoHashUtil.getNeighbors(userGeoHash5));

            List<BranchResponseDTO> candidateBranches =
            companyId != null && !companyId.isBlank()
                ? getAllActiveBranchesByCompany(companyId)
                : getAllActiveBranchesAcrossAllCompanies();

        return candidateBranches.stream()
                .filter(branch -> branch.getLatitude() != null && branch.getLongitude() != null)
                .filter(branch -> {
                if (branch.getGeoHash5() == null || branch.getGeoHash5().isBlank()) {
                    return true;
                }
                return geoCandidates.contains(branch.getGeoHash5());
                })
            .filter(branch -> GeoHashUtil.calculateDistance(
                latitude,
                longitude,
                branch.getLatitude(),
                branch.getLongitude()) <= safeRadiusKm)
            .sorted(Comparator.comparing(branch -> GeoHashUtil.calculateDistance(
                latitude,
                longitude,
                branch.getLatitude(),
                branch.getLongitude())))
            .collect(Collectors.toList());
}

private void applyLocation(Branch branch, Double latitude, Double longitude, Instant now) {
    if (latitude == null && longitude == null) {
        branch.setLatitude(null);
        branch.setLongitude(null);
        branch.setGeoHash5(null);
        branch.setGeoHash6(null);
        branch.setLocationUpdatedAt(null);
        return;
    }

    if (latitude == null || longitude == null) {
        throw new BadRequestException("latitude and longitude must both be provided");
    }

    branch.setLatitude(latitude);
    branch.setLongitude(longitude);
    branch.setGeoHash5(GeoHashUtil.encode(latitude, longitude, 5));
    branch.setGeoHash6(GeoHashUtil.encode(latitude, longitude, 6));
    branch.setLocationUpdatedAt(now);
}

private PublicBranchDTO toPublicBranchDTO(BranchResponseDTO branch) {
    PublicBranchDTO dto = new PublicBranchDTO();
    dto.id = branch.getBranchId();
    dto.companyId = branch.getCompanyId();
    dto.name = branch.getName();
    dto.address = branch.getAddress();
    dto.latitude = branch.getLatitude();
    dto.longitude = branch.getLongitude();
    dto.isActive = branch.getIsActive();
    return dto;
}

}
