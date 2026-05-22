package se.jensen.ali.cloudstore.userorderservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.jensen.ali.cloudstore.userorderservice.user.AppUser;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final String secret;

    public JwtService(@Value("${jwt.secret:cloudstore-development-secret-key}") String secret) {
        this.secret = secret;
    }

    public String generateToken(AppUser user) {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        long expiresAt = Instant.now().plusSeconds(60 * 60 * 24).getEpochSecond();
        String payload = """
                {"sub":"%s","userId":%d,"email":"%s","exp":%d}
                """.formatted(user.getEmail(), user.getId(), user.getEmail(), expiresAt).trim();

        String encodedHeader = encode(header);
        String encodedPayload = encode(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;
        String signature = sign(unsignedToken);

        return unsignedToken + "." + signature;
    }

    public String validateTokenAndGetUsername(String token) {
        String[] parts = token.split("\\.");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);

        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        long expiresAt = Long.parseLong(extractNumberClaim(payload, "exp"));

        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new IllegalArgumentException("JWT has expired");
        }

        return extractStringClaim(payload, "sub");
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
            throw new IllegalStateException("Could not create JWT signature", exception);
        }
    }

    private String extractStringClaim(String payload, String claimName) {
        String marker = "\"" + claimName + "\":\"";
        int start = payload.indexOf(marker);

        if (start == -1) {
            throw new IllegalArgumentException("Missing JWT claim: " + claimName);
        }

        int valueStart = start + marker.length();
        int valueEnd = payload.indexOf("\"", valueStart);

        if (valueEnd == -1) {
            throw new IllegalArgumentException("Invalid JWT claim: " + claimName);
        }

        return payload.substring(valueStart, valueEnd);
    }

    private String extractNumberClaim(String payload, String claimName) {
        String marker = "\"" + claimName + "\":";
        int start = payload.indexOf(marker);

        if (start == -1) {
            throw new IllegalArgumentException("Missing JWT claim: " + claimName);
        }

        int valueStart = start + marker.length();
        int valueEnd = payload.indexOf("}", valueStart);

        if (valueEnd == -1) {
            throw new IllegalArgumentException("Invalid JWT claim: " + claimName);
        }

        return payload.substring(valueStart, valueEnd);
    }
}
