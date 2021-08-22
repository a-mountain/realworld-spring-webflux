package com.realworld.springmongo.user;

public interface UserTokenProvider {
    String getToken(String userId);
}
