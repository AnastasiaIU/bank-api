package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing JWT token after successful login.")
public record LoginResponseDTO(
        @Schema(description = "JWT token to be used for authenticated requests.",
                example = "eyJhbGciOiJSUzM4NCJ9.eyJzdWIiOiIxMjNAbWFpbC5jb20iLCJhdXRoIj......")
        String token) {
}
