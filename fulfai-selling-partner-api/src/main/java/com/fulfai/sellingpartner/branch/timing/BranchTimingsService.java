package com.fulfai.sellingpartner.branch.timing;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fulfai.sellingpartner.branch.Branch;
import com.fulfai.sellingpartner.branch.BranchRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class BranchTimingsService {

    private static final List<String> DAYS = List.of(
            "MONDAY",
            "TUESDAY",
            "WEDNESDAY",
            "THURSDAY",
            "FRIDAY",
            "SATURDAY",
            "SUNDAY"
    );

    @Inject
    BranchRepository branchRepository;

    public BranchTimingsResponseDTO getTimings(String companyId, String branchId) {
        Branch branch = requireBranch(companyId, branchId);
        return toResponse(branch);
    }

    public BranchTimingsResponseDTO updateTimings(
            String companyId,
            String branchId,
            @Valid BranchTimingsUpdateRequestDTO request
    ) {
        Branch branch = requireBranch(companyId, branchId);

        String timezone = normalizeTimezone(request.getTimezone());
        Map<String, DayScheduleDTO> weekly = normalizeWeeklySchedule(request.getWeeklySchedule());

        branch.setTimezone(timezone);
        branch.setWeeklySchedule(toNativeWeeklySchedule(weekly));
        branch.setUpdatedAt(Instant.now());

        branchRepository.save(branch);
        return toResponse(branch);
    }

    public BranchClosureResponseDTO addClosure(
            String companyId,
            String branchId,
            @Valid BranchClosureRequestDTO request
    ) {
        Branch branch = requireBranch(companyId, branchId);

        BranchClosure closure = toClosure(request);
        closure.setId("cl-" + UUID.randomUUID());

        List<BranchClosure> closures = getClosures(branch);
        closures.add(closure);

        branch.setClosures(toNativeClosures(closures));
        branch.setUpdatedAt(Instant.now());

        branchRepository.save(branch);
        return toClosureResponse(closure);
    }

    public BranchClosureResponseDTO updateClosure(
            String companyId,
            String branchId,
            String closureId,
            @Valid BranchClosureRequestDTO request
    ) {
        Branch branch = requireBranch(companyId, branchId);
        List<BranchClosure> closures = getClosures(branch);

        BranchClosure found = closures.stream()
                .filter(c -> closureId.equals(c.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Closure not found with id: " + closureId));

        BranchClosure incoming = toClosure(request);
        found.setDate(incoming.getDate());
        found.setClosedAllDay(incoming.getClosedAllDay());
        found.setOpeningTime(incoming.getOpeningTime());
        found.setClosingTime(incoming.getClosingTime());
        found.setReason(incoming.getReason());

        branch.setClosures(toNativeClosures(closures));
        branch.setUpdatedAt(Instant.now());
        branchRepository.save(branch);

        return toClosureResponse(found);
    }

    public void deleteClosure(String companyId, String branchId, String closureId) {
        Branch branch = requireBranch(companyId, branchId);
        List<BranchClosure> closures = getClosures(branch);

        boolean removed = closures.removeIf(c -> closureId.equals(c.getId()));
        if (!removed) {
            throw new NotFoundException("Closure not found with id: " + closureId);
        }

        branch.setClosures(toNativeClosures(closures));
        branch.setUpdatedAt(Instant.now());
        branchRepository.save(branch);
    }

    private Branch requireBranch(String companyId, String branchId) {
        Branch branch = branchRepository.getById(companyId, branchId);
        if (branch == null) {
            throw new NotFoundException("Branch not found with id: " + branchId);
        }
        return branch;
    }

    private String normalizeTimezone(String timezone) {
        try {
            return ZoneId.of(timezone).getId();
        } catch (Exception ex) {
            throw new BadRequestException("Invalid timezone. Use IANA timezone like Asia/Dubai");
        }
    }

    private Map<String, DayScheduleDTO> normalizeWeeklySchedule(Map<String, DayScheduleDTO> incoming) {
        if (incoming == null) {
            throw new BadRequestException("weeklySchedule is required");
        }

        Map<String, DayScheduleDTO> normalized = new HashMap<>();
        for (String day : DAYS) {
            DayScheduleDTO schedule = incoming.get(day);
            if (schedule == null) {
                throw new BadRequestException("weeklySchedule missing day: " + day);
            }
            validateDaySchedule(day, schedule);
            normalized.put(day, schedule);
        }
        return normalized;
    }

    private void validateDaySchedule(String day, DayScheduleDTO schedule) {
        boolean open = Boolean.TRUE.equals(schedule.getOpen());
        String opening = schedule.getOpeningTime();
        String closing = schedule.getClosingTime();

        if (!open) {
            schedule.setOpeningTime(null);
            schedule.setClosingTime(null);
            return;
        }

        if (opening == null || opening.isBlank() || closing == null || closing.isBlank()) {
            throw new BadRequestException(day + " openingTime and closingTime are required when open=true");
        }

        validateTimeOrder(day, opening, closing);
    }

    private BranchClosure toClosure(BranchClosureRequestDTO request) {
        BranchClosure closure = new BranchClosure();
        closure.setDate(request.getDate());
        closure.setClosedAllDay(Boolean.TRUE.equals(request.getClosedAllDay()));
        closure.setOpeningTime(request.getOpeningTime());
        closure.setClosingTime(request.getClosingTime());
        closure.setReason(request.getReason());

        validateClosure(closure);
        return closure;
    }

    private void validateClosure(BranchClosure closure) {
        try {
            LocalDate.parse(closure.getDate());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Closure date must be valid yyyy-MM-dd");
        }

        if (Boolean.TRUE.equals(closure.getClosedAllDay())) {
            closure.setOpeningTime(null);
            closure.setClosingTime(null);
            return;
        }

        if (closure.getOpeningTime() == null || closure.getOpeningTime().isBlank()
                || closure.getClosingTime() == null || closure.getClosingTime().isBlank()) {
            throw new BadRequestException("Closure openingTime and closingTime are required when closedAllDay=false");
        }

        validateTimeOrder("closure", closure.getOpeningTime(), closure.getClosingTime());
    }

    private void validateTimeOrder(String label, String opening, String closing) {
        try {
            LocalTime open = LocalTime.parse(opening);
            LocalTime close = LocalTime.parse(closing);
            if (!open.isBefore(close)) {
                throw new BadRequestException(label + " openingTime must be before closingTime");
            }
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(label + " times must be valid HH:mm values");
        }
    }

    private BranchTimingsResponseDTO toResponse(Branch branch) {
        BranchTimingsResponseDTO dto = new BranchTimingsResponseDTO();
        dto.setTimezone(branch.getTimezone());
        dto.setWeeklySchedule(getWeeklySchedule(branch));

        List<BranchClosureResponseDTO> closureDTOs = getClosures(branch)
                .stream()
                .map(this::toClosureResponse)
                .toList();
        dto.setClosures(closureDTOs);
        return dto;
    }

    private BranchClosureResponseDTO toClosureResponse(BranchClosure closure) {
        BranchClosureResponseDTO dto = new BranchClosureResponseDTO();
        dto.setId(closure.getId());
        dto.setDate(closure.getDate());
        dto.setClosedAllDay(closure.getClosedAllDay());
        dto.setOpeningTime(closure.getOpeningTime());
        dto.setClosingTime(closure.getClosingTime());
        dto.setReason(closure.getReason());
        return dto;
    }

    private Map<String, DayScheduleDTO> getWeeklySchedule(Branch branch) {
        if (branch.getWeeklySchedule() == null || branch.getWeeklySchedule().isEmpty()) {
            return defaultWeeklySchedule();
        }
        return fromNativeWeeklySchedule(branch.getWeeklySchedule());
    }

    private List<BranchClosure> getClosures(Branch branch) {
        if (branch.getClosures() == null || branch.getClosures().isEmpty()) {
            return new ArrayList<>();
        }
        return fromNativeClosures(branch.getClosures());
    }

    private Map<String, Map<String, String>> toNativeWeeklySchedule(Map<String, DayScheduleDTO> weekly) {
        Map<String, Map<String, String>> nativeMap = new HashMap<>();
        for (String day : DAYS) {
            DayScheduleDTO d = weekly.get(day);
            Map<String, String> perDay = new HashMap<>();
            perDay.put("open", String.valueOf(Boolean.TRUE.equals(d.getOpen())));
            perDay.put("openingTime", Objects.toString(d.getOpeningTime(), ""));
            perDay.put("closingTime", Objects.toString(d.getClosingTime(), ""));
            nativeMap.put(day, perDay);
        }
        return nativeMap;
    }

    private Map<String, DayScheduleDTO> fromNativeWeeklySchedule(Map<String, Map<String, String>> nativeMap) {
        Map<String, DayScheduleDTO> out = new HashMap<>();
        for (String day : DAYS) {
            DayScheduleDTO d = new DayScheduleDTO();
            Map<String, String> perDay = nativeMap.get(day);
            if (perDay == null || perDay.isEmpty()) {
                d.setOpen(false);
            } else {
                String open = perDay.get("open");
                String opening = perDay.get("openingTime");
                String closing = perDay.get("closingTime");
                d.setOpen(Boolean.parseBoolean(open));
                d.setOpeningTime(opening == null || opening.isBlank() ? null : opening);
                d.setClosingTime(closing == null || closing.isBlank() ? null : closing);
            }
            out.put(day, d);
        }
        return out;
    }

    private List<Map<String, String>> toNativeClosures(List<BranchClosure> closures) {
        List<Map<String, String>> out = new ArrayList<>();
        for (BranchClosure closure : closures) {
            Map<String, String> map = new HashMap<>();
            map.put("id", closure.getId());
            map.put("date", closure.getDate());
            map.put("closedAllDay", String.valueOf(Boolean.TRUE.equals(closure.getClosedAllDay())));
            map.put("openingTime", Objects.toString(closure.getOpeningTime(), ""));
            map.put("closingTime", Objects.toString(closure.getClosingTime(), ""));
            map.put("reason", Objects.toString(closure.getReason(), ""));
            out.add(map);
        }
        return out;
    }

    private List<BranchClosure> fromNativeClosures(List<Map<String, String>> nativeList) {
        List<BranchClosure> out = new ArrayList<>();
        for (Map<String, String> map : nativeList) {
            BranchClosure c = new BranchClosure();
            c.setId(map.get("id"));
            c.setDate(map.get("date"));
            c.setClosedAllDay(Boolean.parseBoolean(map.getOrDefault("closedAllDay", "false")));
            String opening = map.get("openingTime");
            String closing = map.get("closingTime");
            String reason = map.get("reason");
            c.setOpeningTime(opening == null || opening.isBlank() ? null : opening);
            c.setClosingTime(closing == null || closing.isBlank() ? null : closing);
            c.setReason(reason == null || reason.isBlank() ? null : reason);
            out.add(c);
        }
        return out;
    }

    private Map<String, DayScheduleDTO> defaultWeeklySchedule() {
        Map<String, DayScheduleDTO> out = new HashMap<>();
        for (String day : DAYS) {
            DayScheduleDTO d = new DayScheduleDTO();
            d.setOpen(false);
            out.put(day, d);
        }
        return out;
    }
}
