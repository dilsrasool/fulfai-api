package com.fulfai.deliverypartner.location;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.deliverypartner.driver.Driver;
import com.fulfai.deliverypartner.driver.DriverRepository;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class LocationService {

    private static final double DEFAULT_RADIUS_KM = 5.0;
    private static final int DEFAULT_MAX_RESULTS = 10;

    @Inject
    LocationRepository locationRepository;

    @Inject
    DriverRepository driverRepository;

    @Inject
    LocationMapper locationMapper;

    /**
     * Record driver location update.
     */
    public LocationResponseDTO updateLocation(String companyId, String driverId,
            @Valid LocationUpdateDTO locationDTO) {
        // Verify driver exists
        Driver driver = driverRepository.getById(companyId, driverId);
        if (driver == null) {
            throw new NotFoundException("Driver not found with id: " + driverId);
        }

        Instant now = Instant.now();
        String geohash = GeoHashUtil.encode(locationDTO.getLatitude(), locationDTO.getLongitude());

        // Create location record
        DriverLocation location = new DriverLocation();
        location.setDriverId(driverId);
        location.setTimestamp(now);
        location.setLatitude(locationDTO.getLatitude());
        location.setLongitude(locationDTO.getLongitude());
        location.setGeohash(geohash);
        location.setAccuracy(locationDTO.getAccuracy());
        location.setSpeed(locationDTO.getSpeed());
        location.setHeading(locationDTO.getHeading());
        location.setStatus(driver.getStatus());

        locationRepository.save(location);

        // Update driver's last known location
        driver.setLastLatitude(locationDTO.getLatitude());
        driver.setLastLongitude(locationDTO.getLongitude());
        driver.setLastLocationUpdate(now);
        driver.setUpdatedAt(now);
        driverRepository.save(driver);

        Log.debugf("Updated location for driver %s: %s", driverId, geohash);

        return locationMapper.toResponseDTO(location);
    }

    /**
     * Get driver's location history.
     */
    public PaginatedResponse<LocationResponseDTO> getDriverLocationHistory(String driverId,
            String nextToken, Integer limit) {
        PaginatedResponse<DriverLocation> response = locationRepository.getByDriver(driverId, nextToken, limit);

        return PaginatedResponse.<LocationResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(locationMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    /**
     * Find nearby drivers using geohash-based proximity search.
     */
    public List<NearbyDriverDTO> findNearbyDrivers(@Valid ProximitySearchDTO searchDTO) {
        double radiusKm = searchDTO.getRadiusKm() != null ? searchDTO.getRadiusKm() : DEFAULT_RADIUS_KM;
        int maxResults = searchDTO.getMaxResults() != null ? searchDTO.getMaxResults() : DEFAULT_MAX_RESULTS;

        // Get geohash for search point
        String centerGeohash = GeoHashUtil.encode(searchDTO.getLatitude(), searchDTO.getLongitude());

        // Get neighboring geohashes to search (covers ~3x3 grid around center)
        List<String> geohashes = GeoHashUtil.getNeighbors(centerGeohash);

        // Query all neighboring geohashes
        List<DriverLocation> locations = locationRepository.getByGeohashes(geohashes, 50);

        // Calculate distances and filter
        return locations.stream()
                // Group by driver and get latest location
                .collect(Collectors.toMap(
                        DriverLocation::getDriverId,
                        loc -> loc,
                        (loc1, loc2) -> loc1.getTimestamp().isAfter(loc2.getTimestamp()) ? loc1 : loc2))
                .values().stream()
                // Filter by status if specified
                .filter(loc -> searchDTO.getStatus() == null || searchDTO.getStatus().equals(loc.getStatus()))
                // Calculate distance and filter by radius
                .map(loc -> {
                    double distance = GeoHashUtil.calculateDistance(
                            searchDTO.getLatitude(), searchDTO.getLongitude(),
                            loc.getLatitude(), loc.getLongitude());

                    NearbyDriverDTO dto = new NearbyDriverDTO();
                    dto.setDriverId(loc.getDriverId());
                    dto.setLatitude(loc.getLatitude());
                    dto.setLongitude(loc.getLongitude());
                    dto.setDistanceKm(Math.round(distance * 100.0) / 100.0);  // Round to 2 decimal places
                    dto.setStatus(loc.getStatus());
                    dto.setLastUpdate(loc.getTimestamp());
                    return dto;
                })
                .filter(dto -> dto.getDistanceKm() <= radiusKm)
                // Sort by distance
                .sorted(Comparator.comparingDouble(NearbyDriverDTO::getDistanceKm))
                // Limit results
                .limit(maxResults)
                .collect(Collectors.toList());
    }
}
