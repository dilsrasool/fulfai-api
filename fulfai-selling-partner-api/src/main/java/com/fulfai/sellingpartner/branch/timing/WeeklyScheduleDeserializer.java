package com.fulfai.sellingpartner.branch.timing;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class WeeklyScheduleDeserializer extends JsonDeserializer<Map<String, DayScheduleDTO>> {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter H_MM_A = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter HH_MM_A = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    @Override
    public Map<String, DayScheduleDTO> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }

        Map<String, DayScheduleDTO> out = new HashMap<>();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String normalizedDay = normalizeDay(entry.getKey());
                if (normalizedDay != null) {
                    out.put(normalizedDay, parseScheduleNode(entry.getValue()));
                }
            });
            return out;
        }

        if (node.isArray()) {
            for (JsonNode row : node) {
                if (!row.isObject()) {
                    continue;
                }

                String dayRaw = pickText(row, "day", "dayName", "dayOfWeek", "name");
                String normalizedDay = normalizeDay(dayRaw);
                if (normalizedDay != null) {
                    out.put(normalizedDay, parseScheduleNode(row));
                }
            }
            return out;
        }

        return null;
    }

    private DayScheduleDTO parseScheduleNode(JsonNode node) {
        DayScheduleDTO dto = new DayScheduleDTO();

        Boolean open = pickBoolean(node, "open", "isOpen", "enabled");
        dto.setOpen(Boolean.TRUE.equals(open));

        String opening = normalizeTime(pickText(node, "openingTime", "openTime", "startTime"));
        String closing = normalizeTime(pickText(node, "closingTime", "closeTime", "endTime"));

        dto.setOpeningTime(opening);
        dto.setClosingTime(closing);
        return dto;
    }

    private String pickText(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode v = node.get(key);
            if (v != null && !v.isNull()) {
                String text = v.asText();
                if (text != null && !text.isBlank()) {
                    return text.trim();
                }
            }
        }
        return null;
    }

    private Boolean pickBoolean(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode v = node.get(key);
            if (v != null && !v.isNull()) {
                if (v.isBoolean()) {
                    return v.asBoolean();
                }
                String text = v.asText();
                if (text != null && !text.isBlank()) {
                    return Boolean.parseBoolean(text.trim());
                }
            }
        }
        return false;
    }

    private String normalizeDay(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        return switch (input.trim().toUpperCase(Locale.ROOT)) {
            case "MONDAY", "MON" -> "MONDAY";
            case "TUESDAY", "TUE", "TUES" -> "TUESDAY";
            case "WEDNESDAY", "WED" -> "WEDNESDAY";
            case "THURSDAY", "THU", "THUR", "THURS" -> "THURSDAY";
            case "FRIDAY", "FRI" -> "FRIDAY";
            case "SATURDAY", "SAT" -> "SATURDAY";
            case "SUNDAY", "SUN" -> "SUNDAY";
            default -> null;
        };
    }

    private String normalizeTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String candidate = raw.trim().toUpperCase(Locale.ROOT);

        try {
            return LocalTime.parse(candidate, HH_MM).format(HH_MM);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalTime.parse(candidate, HH_MM_A).format(HH_MM);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalTime.parse(candidate, H_MM_A).format(HH_MM);
        } catch (DateTimeParseException ignored) {
        }

        return raw;
    }
}
