package org.bcp.dlmsenrolment.mappers;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DtoMapper {

    public static <T> T mapToDto(HashMap<String, Object> body, Class<T> dtoClass) {
        try {
            T dtoInstance = dtoClass.getDeclaredConstructor().newInstance();

            for (Map.Entry<String, Object> entry : body.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                try {
                    Field field = dtoClass.getDeclaredField(fieldName);
                    field.setAccessible(true);

                    // Call the updated convertValue method
                    Object convertedValue = convertValue(value, field.getType());
                    field.set(dtoInstance, convertedValue);

                } catch (NoSuchFieldException e) {
                    // Skip unknown fields
                }
            }

            return dtoInstance;

        } catch (Exception e) {
            // Making the error message more specific
            throw new RuntimeException("Failed to map to DTO " + dtoClass.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType.isInstance(value)) {
            return value;
        }

        // ==========================================================
        //  START: ADDED LOGIC TO HANDLE COLLECTIONS
        // ==========================================================
        if (value instanceof Collection) {
            Collection<?> sourceCollection = (Collection<?>) value;
            if (targetType == Set.class) {
                return new HashSet<>(sourceCollection);
            }
            if (targetType == List.class) {
                return new ArrayList<>(sourceCollection);
            }
        }
        // ==========================================================
        //  END: ADDED LOGIC
        // ==========================================================


        String stringValue = value.toString();

        if (targetType == String.class) {
            return stringValue;
        } else if (targetType.isEnum()) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object enumValue = Enum.valueOf((Class<? extends Enum>) targetType, stringValue.trim().toUpperCase());
            return enumValue;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(stringValue);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(stringValue);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(stringValue);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(stringValue);
        } else if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(stringValue);
        } else if (targetType == Short.class || targetType == short.class) {
            return Short.parseShort(stringValue);
        } else if (targetType == Byte.class || targetType == byte.class) {
            return Byte.parseByte(stringValue);
        } else if (targetType == BigDecimal.class) {
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                return new BigDecimal(stringValue);
            }
        } else if (targetType == LocalDate.class) {
            return LocalDate.parse(stringValue, DateTimeFormatter.ISO_DATE);
        } else if (targetType == LocalDateTime.class) {
            return LocalDateTime.parse(stringValue, DateTimeFormatter.ISO_DATE_TIME);
        }

        throw new IllegalArgumentException("Unsupported target type: " + targetType.getName());
    }
}