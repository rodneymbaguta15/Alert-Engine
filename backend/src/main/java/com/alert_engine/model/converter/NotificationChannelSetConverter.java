package com.alert_engine.model.converter;

import com.alert_engine.model.enums.NotificationChannel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JPA AttributeConverter to persist a Set of NotificationChannel enums as a comma-separated string in the database.
 * <p>
 * The converter handles null and empty sets by persisting them as an empty string. When reading from the database,
 * it converts an empty or blank string back to an empty set. The converter also ensures that any unknown values
 * in the database will result in an IllegalArgumentException, which is intentional to catch data integrity issues.
 */
@Converter
public class NotificationChannelSetConverter
        implements AttributeConverter<Set<NotificationChannel>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(Set<NotificationChannel> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            // Persist empty sets as an empty string; the column is NOT NULL in the schema.
            return "";
        }
        return attribute.stream()
                .map(Enum::name)
                .sorted()                          // deterministic ordering for easier debugging
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public Set<NotificationChannel> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return EnumSet.noneOf(NotificationChannel.class);
        }
        return Arrays.stream(dbData.split(DELIMITER))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(NotificationChannel::valueOf) // throws IllegalArgumentException on unknown values — intentional
                .collect(Collectors.toCollection(
                        () -> EnumSet.noneOf(NotificationChannel.class)));
    }
}