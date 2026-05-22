package se.jensen.ali.cloudstore.userorderservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.jensen.ali.cloudstore.userorderservice.auth.AuthService;
import se.jensen.ali.cloudstore.userorderservice.auth.LoginRequest;
import se.jensen.ali.cloudstore.userorderservice.auth.LoginResponse;
import se.jensen.ali.cloudstore.userorderservice.auth.RegisterRequest;
import se.jensen.ali.cloudstore.userorderservice.security.JwtService;
import se.jensen.ali.cloudstore.userorderservice.user.AppUserRepository;
import se.jensen.ali.cloudstore.userorderservice.user.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
    }

    @Test
    void registerCreatesUser() {
        RegisterRequest request = new RegisterRequest("Ali", "Hassan", "ali@test.com", "0701234567", "password123");

        UserResponse response = authService.register(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.username()).isEqualTo("ali@test.com");
        assertThat(response.email()).isEqualTo("ali@test.com");
        assertThat(response.firstName()).isEqualTo("Ali");
        assertThat(response.lastName()).isEqualTo("Hassan");
        assertThat(response.phoneNumber()).isEqualTo("0701234567");
        assertThat(appUserRepository.existsByEmail("ali@test.com")).isTrue();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("Ali", "Hassan", "ali@test.com", "0701234567", "password123");
        RegisterRequest duplicate = new RegisterRequest("Ali", "Other", "ali@test.com", "0707654321", "password123");

        authService.register(request);

        assertThatThrownBy(() -> authService.register(duplicate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already taken");
    }

    @Test
    void loginReturnsJwtToken() {
        authService.register(new RegisterRequest("Ali", "Hassan", "ali@test.com", "0701234567", "password123"));

        LoginResponse response = authService.login(new LoginRequest("ali@test.com", "password123"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.token().split("\\.")).hasSize(3);
        assertThat(jwtService.validateTokenAndGetUsername(response.token())).isEqualTo("ali@test.com");
    }

    @Test
    void loginRejectsWrongPassword() {
        authService.register(new RegisterRequest("Ali", "Hassan", "ali@test.com", "0701234567", "password123"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ali@test.com", "wrong-password")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void getCurrentUserReturnsUser() {
        authService.register(new RegisterRequest("Ali", "Hassan", "ali@test.com", "0701234567", "password123"));

        UserResponse response = authService.getCurrentUser("ali@test.com");

        assertThat(response.username()).isEqualTo("ali@test.com");
        assertThat(response.email()).isEqualTo("ali@test.com");
        assertThat(response.firstName()).isEqualTo("Ali");
    }
}
