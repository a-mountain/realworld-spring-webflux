package com.realworld.springmongo.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRegistrationRequest {
    @NotBlank
    String username;
    @Email
    @NotBlank
    String email;
    @NotBlank
    String password;

    public User toUser(String encodedPassword, String id) {
        return User.builder()
                .id(id)
                .encodedPassword(encodedPassword)
                .email(email)
                .username(username)
                .build();
    }
}
