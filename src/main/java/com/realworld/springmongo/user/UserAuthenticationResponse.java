package com.realworld.springmongo.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAuthenticationResponse {
    String email;
    String token;
    String username;
    String bio;
    String image;

    public static UserAuthenticationResponse fromUser(User savedUser, String token) {
        return new UserAuthenticationResponse()
                .setUsername(savedUser.getUsername())
                .setEmail(savedUser.getEmail())
                .setBio("")
                .setImage(null)
                .setToken(token);
    }
}
