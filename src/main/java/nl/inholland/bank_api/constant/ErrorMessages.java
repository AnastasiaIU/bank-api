package nl.inholland.bank_api.constant;

public class ErrorMessages {
    public static final String EMAIL_EXISTS = "Email already exists";
    public static final String BSN_EXISTS = "BSN already exists";

    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String FIRST_NAME_REQUIRED = "First name is required";
    public static final String LAST_NAME_REQUIRED = "Last name is required";
    public static final String BSN_REQUIRED = "BSN is required";
    public static final String IBAN_REQUIRED = "Account number (IBAN) is required";
    public static final String PHONE_REQUIRED = "Phone number is required";
    public static final String TRANSACTION_TYPE_REQUIRED = "Transaction type is required";
    public static final String AMOUNT_REQUIRED = "Amount is required";
    public static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";

    public static final String INVALID_EMAIL_FORMAT = "Invalid email format";
    public static final String INVALID_BSN_FORMAT = "BSN must be exactly 9 digits";
    public static final String INVALID_IBAN_FORMAT = "Invalid IBAN format";
    public static final String INVALID_PHONE_FORMAT = "Phone number must be 10–15 digits (optionally starts with +)";
    public static final String AMOUNT_MINIMUM = "Amount must be greater than 0";
    public static final String DAILY_LIMIT_MINIMUM = "Daily limit must be greater than 0";
    public static final String WITHDRAW_LIMIT_MINIMUM = "Withdraw limit must be greater than 0";

    public static final String UNHANDLED_EXCEPTION = "Unhandled exception";
    public static final String UNEXPECTED_ERROR = "Unexpected error occurred";
    public static final String DATA_INTEGRITY_VIOLATION = "Data Integrity Violation";
    public static final String HTTP_METHOD_NOT_SUPPORTED = "HTTP method not supported";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String TRANSACTION_NOT_FOUND = "Transaction not found";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String ACCOUNT_NOT_FOUND = "Account not found";
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance";
    public static final String DAILY_WITHDRAWAL_LIMIT_EXCEEDED = "Daily withdrawal limit exceeded";
    public static final String REQUEST_PAYLOAD_TOO_LARGE = "Request payload too large";
    public static final String MISSING_TOKEN_OR_AUTHORIZATION_HEADER = "Missing token or Authorization header";
    public static final String EXPIRED_TOKEN = "Expired Token";
    public static final String MALFORMED_TOKEN = "Malformed Token";
    public static final String INVALID_TOKEN_SIGNATURE = "Invalid token signature";
    public static final String UNSUPPORTED_TOKEN = "Unsupported token";

    public static final String UNKNOWN_ERROR_KEY = "Unknown error key";
}