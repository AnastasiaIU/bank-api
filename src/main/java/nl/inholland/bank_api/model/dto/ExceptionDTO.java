package nl.inholland.bank_api.model.dto;

import java.util.List;

public record ExceptionDTO(int status, String exception, List<String> message) {
}
