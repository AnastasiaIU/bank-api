package nl.inholland.bank_api.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import javax.annotation.PostConstruct;

@Component
public class JwtKeyProvider {
    @Value("${jwt.key-store}")
    private String keystore;

    @Value("${jwt.key-store-password}")
    private String password;

    @Value("${jwt.key-alias}")
    private String alias;

    @Getter
    private Key privateKey;

    @Getter
    private PublicKey publicKey;

    @PostConstruct
    protected void init() {
        try {
            ClassPathResource resource = new ClassPathResource(keystore);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(resource.getInputStream(), password.toCharArray());
            privateKey = keyStore.getKey(alias, password.toCharArray());
            Certificate cert = keyStore.getCertificate(alias);
            publicKey = cert.getPublicKey();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
