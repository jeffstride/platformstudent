package com.mrstride;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * This is a Singleton Pattern. This is a Component that will get automatically
 * registered by the IoC SpringBoot framework. The ApplicationContext object
 * will be saved an made available via the static field. This will allow all
 * other objects, Component or not, to use IoC to fulfill dependencies. Example:
 * 
 *    imageService = ApplicationContextProvider.getApplicationContext().getBean(ImageService.class);
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }
}