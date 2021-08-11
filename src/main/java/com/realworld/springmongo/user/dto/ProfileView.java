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

    public static ProfileView profileViewOf(User userToMakeView, boolean following) {
        return new ProfileView()
                .setUsername(userToMakeView.getUsername())
                .setBio(userToMakeView.getBio())
                .setImage(userToMakeView.getImage())
                .setFollowing(following);
    }

    public static ProfileView unfollowedProfileView(User userToMakeView) {
        return profileViewOf(userToMakeView, false);
    }

    public static ProfileView followedProfileViewOf(User userToMakeView) {
        return profileViewOf(userToMakeView, true);
    }

    public static ProfileView ownProfile(User user) {
        return profileViewForViewer(user, user);
    }

    public static ProfileView profileViewForViewer(User userToMakeView, User viewer) {
        return profileViewOf(userToMakeView, userToMakeView.isFollower(viewer));
    }
}
