package nl.inholland.bank_api.util;

public class StringUtils {
    public static String capitalize(String str) {
        if (str == null || str.isBlank()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
