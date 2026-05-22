package se.jensen.ali.cloudstore.userorderservice.user;

public record UserResponse(
        Long id,
        String username,
        String email
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}
