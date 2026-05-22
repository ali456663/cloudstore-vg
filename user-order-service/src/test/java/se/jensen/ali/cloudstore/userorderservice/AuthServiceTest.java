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
        RegisterRequest request = new RegisterRequest("ali", "ali@test.com", "password123");

        UserResponse response = authService.register(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.username()).isEqualTo("ali");
        assertThat(response.email()).isEqualTo("ali@test.com");
        assertThat(appUserRepository.existsByUsername("ali")).isTrue();
    }

    @Test
    void registerRejectsDuplicateUsername() {
        RegisterRequest request = new RegisterRequest("ali", "ali@test.com", "password123");
        RegisterRequest duplicate = new RegisterRequest("ali", "other@test.com", "password123");

        authService.register(request);

        assertThatThrownBy(() -> authService.register(duplicate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is already taken");
    }

    @Test
    void loginReturnsJwtToken() {
        authService.register(new RegisterRequest("ali", "ali@test.com", "password123"));

        LoginResponse response = authService.login(new LoginRequest("ali", "password123"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.token().split("\\.")).hasSize(3);
        assertThat(jwtService.validateTokenAndGetUsername(response.token())).isEqualTo("ali");
    }

    @Test
    void loginRejectsWrongPassword() {
        authService.register(new RegisterRequest("ali", "ali@test.com", "password123"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ali", "wrong-password")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void getCurrentUserReturnsUser() {
        authService.register(new RegisterRequest("ali", "ali@test.com", "password123"));

        UserResponse response = authService.getCurrentUser("ali");

        assertThat(response.username()).isEqualTo("ali");
        assertThat(response.email()).isEqualTo("ali@test.com");
    }
}
