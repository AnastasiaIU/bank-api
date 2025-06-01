package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing JWT token after successful login.")
public record LoginResponseDTO(
        @Schema(description = "JWT token to be used for authenticated requests.",
                example = "eyJhbGciOiJSUzM4NCJ9.eyJzdWIiOiIxMjNAbWFpbC5jb20iLCJhdXRoIjoiQ1VTVE9NRVIiLCJ1c2VySWQiOjIsImlhdCI6MTc0ODc4NDI5MiwiZXhwIjoxNzQ4Nzg3ODkyfQ.O0yMRI9xqYh_QgB2V4bY1xpI73mr4EHrgtxA_7hnZP4NBZtO8m4eDeSxCF-GItWP_beM73PY-k--R_W4OiNu_jMFfJpnPm_M2bST6ALK3IsSQWKKuWm5DdpRyN-Tl8Km4kGbLZoN6Eiw1rrycYoG1JC5ccc0pXOWtjZ-5-lZi3SSLE1TQA39PK7xmRXcSIKU90PL-rAzPHN8YmmGwxY97dzuq7zFb1hZAZPr2_Sddq5Wy4mAeEG8QiTTYCywcw0bdgmr7VAjNSj-llyxCzn7g3OfchKUpt4y3mNmNgldBloeplYy3aDX3zrijxlBc2qZEKvDRq7b-1Fwvq2FBbtH1eo14JA0xTFMl0MYTMnJoe97eK7l2KdQyZPnDnGKxRUWel4a1pbkzPaYb-KNTw2yJiNQ93DFrZvwOd1XIX3sb6Csm4LK4L3PAs65MsUaelEXvSz5cvmdZgEIxKL16rHZdWim18_SBPX6ulwJzvJYzr0wItpiqF5EEfNqNItZY8S3")
        String token) {
}
