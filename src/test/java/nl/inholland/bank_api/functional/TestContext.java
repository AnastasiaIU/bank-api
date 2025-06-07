package nl.inholland.bank_api.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Data
@Component
public class TestContext {
    @Autowired
    private ObjectMapper objectMapper;

    private ResponseEntity<String> response;
    private HttpHeaders headers;
}
