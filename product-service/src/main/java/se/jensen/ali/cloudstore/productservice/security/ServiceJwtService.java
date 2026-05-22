package se.jensen.ali.cloudstore.productservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class ServiceJwtService {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final String secret;

    public ServiceJwtService(@Value("${service.jwt.secret:cloudstore-service-secret-key}") String secret) {
        this.secret = secret;
    }

    public boolean isValid(String token) {
        try {
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                return false;
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);

            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return payload.contains("\"iss\":\"user-order-service\"")
                    && Instant.now().getEpochSecond() <= extractExpiration(payload);
        } catch (Exception exception) {
            return false;
        }
    }

    private long extractExpiration(String payload) {
        String marker = "\"exp\":";
        int start = payload.indexOf(marker);
        int end = payload.indexOf("}", start);

        if (start == -1 || end == -1) {
            throw new IllegalArgumentException("Missing exp claim");
        }

        return Long.parseLong(payload.substring(start + marker.length(), end));
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create JWT signature", exception);
        }
    }
}
