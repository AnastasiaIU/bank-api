package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Represents a standardized error response")
public record ExceptionDTO(
        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "Exception type name", example = "MethodArgumentNotValidException")
        String exception,

        @Schema(description = "List of error messages or validation issues")
        List<String> message
) {
}
