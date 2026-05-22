package se.jensen.ali.cloudstore.userorderservice.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 2, max = 50)
        String firstName,

        @NotBlank
        @Size(min = 2, max = 50)
        String lastName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 7, max = 30)
        String phoneNumber,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
}
