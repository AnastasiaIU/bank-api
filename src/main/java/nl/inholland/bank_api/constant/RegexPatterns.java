package nl.inholland.bank_api.constant;

public class RegexPatterns {
    public static final String IBAN = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$";
    public static final String BSN = "\\d{9}";
    public static final String PHONE = "^\\+?[0-9]{10,15}$";
}
