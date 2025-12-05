package com.fulfai.deliverypartner.assignment;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.deliverypartner.driver.Driver;
import com.fulfai.deliverypartner.driver.DriverRepository;
import com.fulfai.deliverypartner.driver.DriverStatus;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class AssignmentService {

    @Inject
    AssignmentRepository assignmentRepository;

    @Inject
    DriverRepository driverRepository;

    @Inject
    AssignmentMapper assignmentMapper;

    public AssignmentResponseDTO assignOrder(String companyId, String driverId,
            @Valid AssignmentRequestDTO assignmentDTO) {
        // Verify driver exists and is available
        Driver driver = driverRepository.getById(companyId, driverId);
        if (driver == null) {
            throw new NotFoundException("Driver not found with id: " + driverId);
        }

        if (!DriverStatus.AVAILABLE.name().equals(driver.getStatus())) {
            throw new BadRequestException("Driver is not available. Current status: " + driver.getStatus());
        }

        Instant now = Instant.now();

        // Create assignment
        DriverOrderAssignment assignment = assignmentMapper.toEntity(assignmentDTO);
        assignment.setDriverId(driverId);
        assignment.setAssignedAt(now);
        assignment.setStatus(AssignmentStatus.ASSIGNED.name());
        assignment.setCreatedAt(now);
        assignment.setUpdatedAt(now);

        assignmentRepository.save(assignment);

        // Update driver status to BUSY
        driver.setStatus(DriverStatus.BUSY.name());
        driver.setUpdatedAt(now);
        driverRepository.save(driver);

        Log.debugf("Assigned order %s to driver %s", assignmentDTO.getOrderId(), driverId);

        return assignmentMapper.toResponseDTO(assignment);
    }

    public AssignmentResponseDTO updateAssignmentStatus(String driverId, String orderId, String newStatus) {
        AssignmentStatus status = AssignmentStatus.fromString(newStatus);
        if (status == null) {
            throw new BadRequestException("Invalid status: " + newStatus);
        }

        // Find the assignment by order
        PaginatedResponse<DriverOrderAssignment> response = assignmentRepository.getByOrder(orderId, null, 1);
        if (response.getItems().isEmpty()) {
            throw new NotFoundException("Assignment not found for order: " + orderId);
        }

        DriverOrderAssignment assignment = response.getItems().get(0);
        if (!assignment.getDriverId().equals(driverId)) {
            throw new BadRequestException("Order is not assigned to this driver");
        }

        // Validate status transition
        List<String> allowedFrom = AssignmentStatus.getAllowedFromStatuses(newStatus);
        if (!allowedFrom.isEmpty() && !allowedFrom.contains(assignment.getStatus())) {
            throw new BadRequestException("Invalid status transition from " + assignment.getStatus() + " to " + newStatus);
        }

        Instant now = Instant.now();
        assignment.setStatus(status.name());
        assignment.setUpdatedAt(now);

        // Set timestamps based on status
        if (status == AssignmentStatus.PICKED_UP) {
            assignment.setPickedUpAt(now);
        } else if (status == AssignmentStatus.DELIVERED) {
            assignment.setDeliveredAt(now);
            // Set driver back to AVAILABLE
            updateDriverStatusAfterDelivery(assignment.getDriverId());
        } else if (status == AssignmentStatus.CANCELLED) {
            // Set driver back to AVAILABLE
            updateDriverStatusAfterDelivery(assignment.getDriverId());
        }

        assignmentRepository.save(assignment);
        Log.debugf("Updated assignment status for order %s to %s", orderId, newStatus);

        return assignmentMapper.toResponseDTO(assignment);
    }

    private void updateDriverStatusAfterDelivery(String driverId) {
        // Find driver by querying (we need companyId, but we can search by status)
        PaginatedResponse<Driver> drivers = driverRepository.getByStatus(DriverStatus.BUSY.name(), null, 100);
        for (Driver driver : drivers.getItems()) {
            if (driver.getDriverId().equals(driverId)) {
                driver.setStatus(DriverStatus.AVAILABLE.name());
                driver.setUpdatedAt(Instant.now());
                driverRepository.save(driver);
                break;
            }
        }
    }

    public PaginatedResponse<AssignmentResponseDTO> getDriverAssignments(String driverId, String nextToken, Integer limit) {
        PaginatedResponse<DriverOrderAssignment> response = assignmentRepository.getByDriver(driverId, nextToken, limit);

        return PaginatedResponse.<AssignmentResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(assignmentMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public AssignmentResponseDTO getAssignmentByOrder(String orderId) {
        PaginatedResponse<DriverOrderAssignment> response = assignmentRepository.getByOrder(orderId, null, 1);
        if (response.getItems().isEmpty()) {
            throw new NotFoundException("Assignment not found for order: " + orderId);
        }
        return assignmentMapper.toResponseDTO(response.getItems().get(0));
    }

    public PaginatedResponse<AssignmentResponseDTO> getAssignmentsByStatus(String status, String nextToken, Integer limit) {
        if (AssignmentStatus.fromString(status) == null) {
            throw new BadRequestException("Invalid status: " + status);
        }

        PaginatedResponse<DriverOrderAssignment> response = assignmentRepository.getByStatus(status, nextToken, limit);

        return PaginatedResponse.<AssignmentResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(assignmentMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }
}
