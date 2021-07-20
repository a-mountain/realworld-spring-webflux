package com.realworld.springmongo.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserWithToken {
    String email;
    String token;
    String username;
    String bio;
    String image;

    public static UserWithToken fromUser(User savedUser, String token) {
        return new UserWithToken()
                .setUsername(savedUser.getUsername())
                .setEmail(savedUser.getEmail())
                .setBio(savedUser.getBio())
                .setImage(savedUser.getImage())
                .setToken(token);
    }
}
