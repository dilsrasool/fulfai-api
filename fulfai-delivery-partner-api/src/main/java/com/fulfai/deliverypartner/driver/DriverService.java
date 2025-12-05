package com.fulfai.deliverypartner.driver;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class DriverService {

    @Inject
    DriverRepository driverRepository;

    @Inject
    DriverMapper driverMapper;

    public DriverResponseDTO createDriver(String companyId, @Valid DriverRequestDTO driverDTO) {
        Driver driver = driverMapper.toEntity(driverDTO);

        Instant now = Instant.now();
        driver.setCompanyId(companyId);
        driver.setDriverId(UUID.randomUUID().toString());
        driver.setStatus(DriverStatus.OFFLINE.name());
        driver.setCreatedAt(now);
        driver.setUpdatedAt(now);

        if (driver.getIsActive() == null) {
            driver.setIsActive(true);
        }

        driverRepository.save(driver);
        Log.debugf("Created driver with id: %s for company: %s", driver.getDriverId(), companyId);

        return driverMapper.toResponseDTO(driver);
    }

    public DriverResponseDTO getDriverById(String companyId, String driverId) {
        Driver driver = driverRepository.getById(companyId, driverId);
        if (driver != null) {
            return driverMapper.toResponseDTO(driver);
        }
        throw new NotFoundException("Driver not found with id: " + driverId);
    }

    public PaginatedResponse<DriverResponseDTO> getDriversByCompany(String companyId, String nextToken, Integer limit) {
        PaginatedResponse<Driver> response = driverRepository.getByCompany(companyId, nextToken, limit);

        return PaginatedResponse.<DriverResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(driverMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public PaginatedResponse<DriverResponseDTO> getDriversByStatus(String status, String nextToken, Integer limit) {
        if (DriverStatus.fromString(status) == null) {
            throw new BadRequestException("Invalid status: " + status);
        }

        PaginatedResponse<Driver> response = driverRepository.getByStatus(status, nextToken, limit);

        return PaginatedResponse.<DriverResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(driverMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public DriverResponseDTO updateDriver(String companyId, String driverId, @Valid DriverRequestDTO driverDTO) {
        Driver existingDriver = driverRepository.getById(companyId, driverId);
        if (existingDriver == null) {
            throw new NotFoundException("Driver not found with id: " + driverId);
        }

        Driver driver = driverMapper.toEntity(driverDTO);
        driver.setCompanyId(companyId);
        driver.setDriverId(driverId);
        driver.setStatus(existingDriver.getStatus());
        driver.setLastLatitude(existingDriver.getLastLatitude());
        driver.setLastLongitude(existingDriver.getLastLongitude());
        driver.setLastLocationUpdate(existingDriver.getLastLocationUpdate());
        driver.setCreatedAt(existingDriver.getCreatedAt());
        driver.setUpdatedAt(Instant.now());

        driverRepository.save(driver);
        Log.debugf("Updated driver with id: %s", driverId);

        return driverMapper.toResponseDTO(driver);
    }

    public DriverResponseDTO updateDriverStatus(String companyId, String driverId, String newStatus) {
        DriverStatus status = DriverStatus.fromString(newStatus);
        if (status == null) {
            throw new BadRequestException("Invalid status: " + newStatus);
        }

        Driver driver = driverRepository.getById(companyId, driverId);
        if (driver == null) {
            throw new NotFoundException("Driver not found with id: " + driverId);
        }

        driver.setStatus(status.name());
        driver.setUpdatedAt(Instant.now());

        driverRepository.save(driver);
        Log.debugf("Updated driver %s status to: %s", driverId, newStatus);

        return driverMapper.toResponseDTO(driver);
    }

    public void deleteDriver(String companyId, String driverId) {
        Driver driver = driverRepository.getById(companyId, driverId);
        if (driver == null) {
            throw new NotFoundException("Driver not found with id: " + driverId);
        }

        driverRepository.delete(companyId, driverId);
        Log.debugf("Deleted driver with id: %s", driverId);
    }
}
