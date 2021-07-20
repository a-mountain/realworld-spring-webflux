package com.realworld.springmongo.helpers;

import java.util.function.Consumer;

public class Helpers {
    private Helpers() {
    }

    public static <T> void doIfPresent(T value, Consumer<T> doHandler) {
        if (value != null) {
            doHandler.accept(value);
        }
    }

    public static <T> void doIfPresent(T value, Runnable doHandler) {
        if (value != null) {
            doHandler.run();
        }
    }
}
