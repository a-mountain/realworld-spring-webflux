package com.realworld.springmongo.user;

@FunctionalInterface
public interface UserTokenProvider {
    String getToken(String userId);
}
