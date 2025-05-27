package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after successful registration")
public record RegisterResponseDTO(
        @Schema(description = "Unique ID of the newly registered user", example = "42")
        Long id
) {
}
