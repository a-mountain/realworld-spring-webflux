package com.realworld.springmongo.user.dto;

import com.realworld.springmongo.user.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileView {
    String username;

    String bio;

    String image;

    boolean following;

    public static ProfileView toUnfollowedProfileView(User userToMakeView) {
        return toProfileView(userToMakeView, false);
    }

    public static ProfileView toFollowedProfileView(User userToMakeView) {
        return toProfileView(userToMakeView, true);
    }

    public static ProfileView toOwnProfile(User user) {
        return toProfileViewForViewer(user, user);
    }

    public static ProfileView toProfileViewForViewer(User userToMakeView, User viewer) {
        var following = userToMakeView.isFollower(viewer);
        return toProfileView(userToMakeView, following);
    }

    private static ProfileView toProfileView(User userToMakeView, boolean following) {
        return new ProfileView()
                .setUsername(userToMakeView.getUsername())
                .setBio(userToMakeView.getBio())
                .setImage(userToMakeView.getImage())
                .setFollowing(following);
    }
}
