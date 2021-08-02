package com.realworld.springmongo.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileDto {
    String username;

    String bio;

    String image;

    boolean following;

    public static ProfileDto fromUser(User user, boolean following) {
        return new ProfileDto()
                .setUsername(user.getUsername())
                .setBio(user.getBio())
                .setImage(user.getImage())
                .setFollowing(following);
    }
}
