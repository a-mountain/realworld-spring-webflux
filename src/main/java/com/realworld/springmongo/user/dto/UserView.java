package com.realworld.springmongo.user.dto;

import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.UserContext;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserView {
    String email;

    String token;

    String username;

    String bio;

    String image;

    public static UserView fromUserAndToken(UserContext.UserAndToken userAndToken) {
        var user = userAndToken.user();
        var token = userAndToken.token();
        return new UserView()
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setBio(user.getBio())
                .setImage(user.getImage())
                .setToken(token);
    }

    public static UserView fromUserAndToken(User user, String token) {
        return fromUserAndToken(new UserContext.UserAndToken(user, token));
    }
}