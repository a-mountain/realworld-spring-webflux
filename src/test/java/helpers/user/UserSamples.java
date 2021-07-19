package helpers.user;

import com.realworld.springmongo.user.PasswordService;
import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.UserAuthenticationRequest;
import com.realworld.springmongo.user.UserRegistrationRequest;

import java.util.UUID;

public class UserSamples {

    public static final String SAMPLE_USERNAME = "Test username";
    public static final String SAMPLE_EMAIL = "testemail@gmail.com";
    public static final String SAMPLE_PASSWORD = "testpassword";
    public static final String SAMPLE_USER_ID = UUID.randomUUID().toString();

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
                .encodedPassword(encodePassword);
    }
}
