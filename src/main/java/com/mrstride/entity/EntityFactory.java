package com.mrstride.entity;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mrstride.services.AnimationFactory;
import com.mrstride.services.ImageService;

/**
 * This EntityFactory will create any Entity or subclass of Entity.
 * 
 * Mr. Stride used Reflection so that the code is easier to maintain over time.
 * Furthermore, using variable args is useful here, but not required.
 * It is easy and acceptable to simply create each Entity explicitly.
 * 
 * Each entity will have its dependencies injects using a Setter Injection
 * approach. This injection is done manually by calling entity.init();
 * 
 * Each entity must have its init() method called after the dependencies
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
     * are injected using the Constructor Injection method.
     */
    @Autowired
    public EntityFactory(AnimationFactory factory, ImageService imgService) {
        this.factory = factory;
        this.imgService = imgService;
    }

    /**
     * TODO: Implement a create method to create and initialize an Entity.
     * This create method will be called from the DataServiceProvider.
     */

    // Example only. Should be updated or replaced.
    public Entity create(String type, String id, int x, int y, int width, int height, Map<String, Object> properties) {
        Entity e = null;
        if (type.equals("Entity")) {
            e = new Entity(id, x, y, width, height, properties);
        }
        // TODO: handle more types

        // initialize the entity
        if (e != null) {
            e.setServices(factory, imgService, this);
            e.init();
        }
        return e;
    }

}
