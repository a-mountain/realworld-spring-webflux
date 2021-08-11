package helpers.user;

import com.realworld.springmongo.user.PasswordService;
import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.dto.UpdateUserRequest;
import com.realworld.springmongo.user.dto.UserAuthenticationRequest;
import com.realworld.springmongo.user.dto.UserRegistrationRequest;

import java.util.ArrayList;
import java.util.UUID;

public class UserSamples {

    public static final String SAMPLE_USERNAME = "Test username";
    public static final String SAMPLE_EMAIL = "testemail@gmail.com";
    public static final String SAMPLE_PASSWORD = "testpassword";
    public static final String SAMPLE_USER_ID = UUID.randomUUID().toString();
    private static final PasswordService passwordService = new PasswordService();

    public static UserRegistrationRequest sampleUserRegistrationRequest() {
        return new UserRegistrationRequest()
                .setUsername(SAMPLE_USERNAME)
                .setEmail(SAMPLE_EMAIL)
                .setPassword(SAMPLE_PASSWORD);
    }

    public static UserAuthenticationRequest sampleUserAuthenticationRequest() {
        return new UserAuthenticationRequest()
                .setEmail(SAMPLE_EMAIL)
                .setPassword(SAMPLE_PASSWORD);
    }

    public static User.UserBuilder sampleUser(PasswordService passwordService) {
        var encodePassword = passwordService.encodePassword(SAMPLE_PASSWORD);
        return User.builder()
                .id(SAMPLE_USER_ID)
                .username(SAMPLE_USERNAME)
                .email(SAMPLE_EMAIL)
                .encodedPassword(encodePassword)
                .image("test image url")
                .bio("test bio")
                .followingIds(new ArrayList<>());
    }

    public static User.UserBuilder sampleUser() {
        return sampleUser(passwordService);
    }

    public static UpdateUserRequest sampleUpdateUserRequest() {
        return new UpdateUserRequest()
                .setBio("new bio")
                .setEmail("newemail@gmail.com")
                .setImage("new image")
                .setUsername("new username")
                .setPassword("new password");
    }
}
