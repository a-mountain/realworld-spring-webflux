package com.realworld.springmongo.user.dto;

import com.realworld.springmongo.validation.NotBlankOrNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserRequest {
    @Email
    @NotBlankOrNull
    String email;

    @NotBlankOrNull
    String username;

    @NotBlankOrNull
    String password;

    String image;

    String bio;
}
