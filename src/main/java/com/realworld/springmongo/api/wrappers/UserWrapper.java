package com.realworld.springmongo.api.wrappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.realworld.springmongo.user.dto.UpdateUserRequest;
import com.realworld.springmongo.user.dto.UserAuthenticationRequest;
import com.realworld.springmongo.user.dto.UserRegistrationRequest;
import com.realworld.springmongo.user.dto.UserView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class UserWrapper<T> {
    @JsonProperty("user")
    T content;

    @NoArgsConstructor
    public static class UserViewWrapper extends UserWrapper<UserView> {
        public UserViewWrapper(UserView userView) {
            super(userView);
        }
    }

    @NoArgsConstructor
    public static class UserRegistrationRequestWrapper extends UserWrapper<UserRegistrationRequest> {
        public UserRegistrationRequestWrapper(UserRegistrationRequest user) {
            super(user);
        }
    }

    @NoArgsConstructor
    public static class UserAuthenticationRequestWrapper extends UserWrapper<UserAuthenticationRequest> {
        public UserAuthenticationRequestWrapper(UserAuthenticationRequest user) {
            super(user);
        }
    }

    @NoArgsConstructor
    public static class UpdateUserRequestWrapper extends UserWrapper<UpdateUserRequest> {
        public UpdateUserRequestWrapper(UpdateUserRequest user) {
            super(user);
        }
    }
}
