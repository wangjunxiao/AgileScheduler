package cn.dlut.core.rpc.service.handlers;

import java.util.Map;
import cn.dlut.exceptions.MissingRequiredField;

/**
 * Utility class that implements various checks
 * to validate API input.
 */
public final class HandlerUtils {

    /**
     * Implements no-op private constructor.
     * Needed for checkstyle.
     */
    private HandlerUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T fetchField(final String fieldName,
            final Map<String, Object> map,
            /* Class<T> type, */final boolean required, final T def)
                    throws ClassCastException, MissingRequiredField {
        final Object field = map.get(fieldName);
        if (field == null) {
            if (required) {
                throw new MissingRequiredField(fieldName);
            } else {
                return def;
            }
        }
        /*
         * if (field.getClass().isAssignableFrom()) return type.cast(field);
         */
        return (T) field;
        // throw new UnknownFieldType(fieldName, type.getName());
    }

}
