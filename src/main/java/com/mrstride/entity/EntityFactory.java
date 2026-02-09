package com.mrstride.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mrstride.services.AnimationFactory;
import com.mrstride.services.ImageService;

/**
 * This EntityFactory will create any Entity or superclass of Entity.
 * It will create the class via Reflection so that many classes 
 * can be added without having to modify this class.
 * 
 * Each entity will have its dependencies injects via a Setter Injection
 * style. This injection is done manually.
 * 
 * Each entity will have its init() method called after the dependencies
 * have been injected.
 */
@Component
public class EntityFactory {

    /**
     * All dependencies that are to be injected into the Entities
     * are declared as fields here.
     */
    private final AnimationFactory factory;
    private final ImageService imgService;

    /**
     * All dependencies that are to be injected into the Entities
     * are injected via the Constructor Injection method.
     */
    @Autowired
    public EntityFactory(AnimationFactory factory, ImageService imgService) {
        this.factory = factory;
        this.imgService = imgService;
    }
   
    /**
     * Create and initialize an Entity object (or any superclass).
     * 
     * @param <T> Must extend Entity
     * @param type The static class. Example: Entity.class
     * @param ctorArgs The variable number of arguments to use in the constructor.
     * @return A fully initialized Entity (or superclass) object.
     */
    public <T extends Entity> Entity create(Class<T> type, Object... ctorArgs) {
        Entity e = instantiate(type, ctorArgs);

        // all Entities need be manually initialized
        e.setServices(factory, imgService, this);
        e.init();

        return e;
    }

    private <T extends Entity> Entity instantiate(Class<T> type, Object... args) {
        try {
            // pick the matching constructor by count of arguments
            for (var ctor : type.getDeclaredConstructors()) {
                if (ctor.getParameterCount() == args.length) {
                    ctor.setAccessible(true);
                    return (Entity) ctor.newInstance(args);
                }
            }
            throw new IllegalArgumentException("No matching constructor for " + type);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create instance of " + type, e);
        }
    }
}
