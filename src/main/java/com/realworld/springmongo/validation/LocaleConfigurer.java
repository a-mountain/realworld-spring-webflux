package com.realworld.springmongo.validation;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocaleConfigurer implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * Makes hibernate validation always use English default messages
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Locale.setDefault(Locale.ENGLISH);
    }
}
