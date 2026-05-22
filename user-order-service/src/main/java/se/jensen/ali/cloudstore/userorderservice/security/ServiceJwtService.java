package se.jensen.ali.cloudstore.userorderservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class ServiceJwtService {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final String secret;

    public ServiceJwtService(@Value("${service.jwt.secret:cloudstore-service-secret-key}") String secret) {
        this.secret = secret;
    }

    public String createServiceToken() {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        long expiresAt = Instant.now().plusSeconds(60).getEpochSecond();
        String payload = """
                {"iss":"user-order-service","aud":"product-service","exp":%d}
                """.formatted(expiresAt).trim();

        String encodedHeader = encode(header);
        String encodedPayload = encode(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;

        return unsignedToken + "." + sign(unsignedToken);
    }

    private String encode(String value) {
        return BASE64_URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create service JWT", exception);
        }
    }
}
