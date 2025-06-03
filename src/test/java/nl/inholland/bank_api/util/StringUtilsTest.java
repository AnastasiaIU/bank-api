package nl.inholland.bank_api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {
    @Test
    void capitalizeReturnsCapitalizedWord() {
        assertEquals("Test", StringUtils.capitalize("test"));
        assertEquals("Hello", StringUtils.capitalize("hELLo"));
    }

    @Test
    void capitalizeReturnsInputWhenBlank() {
        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
    }

    @Test
    void fieldErrorFormatsMessage() {
        assertEquals("field: message", StringUtils.fieldError("field", "message"));
    }
}