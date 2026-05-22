package se.jensen.ali.cloudstore.userorderservice.auth;

import se.jensen.ali.cloudstore.userorderservice.security.JwtService;
import se.jensen.ali.cloudstore.userorderservice.user.AppUser;
import se.jensen.ali.cloudstore.userorderservice.user.AppUserRepository;
import se.jensen.ali.cloudstore.userorderservice.user.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        AppUser user = new AppUser(
                request.email(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                passwordEncoder.encode(request.password())
        );

        AppUser savedUser = appUserRepository.save(user);
        return UserResponse.from(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return new LoginResponse(jwtService.generateToken(user));
    }

    public UserResponse getCurrentUser(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return UserResponse.from(user);
    }
}
