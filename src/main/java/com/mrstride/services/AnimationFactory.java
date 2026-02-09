package com.mrstride.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnimationFactory {

    private final ImageService imageService;

    @Autowired
    public AnimationFactory(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Gets an Animation object that has access to the Ticker.
     * Does not have access to services or do anything else.
     * 
     * @return Vacuous Animation object
     */
    public static Animation getTickTracker() {
        return new Animation(null, null, 0, 0);
    }

    /**
     * Creates an Animation object that is autowired
     * 
     * @param spriteSheet the ID of the spritesheet to use
     * @param width The width that the animation should be displayed as on the screen
     * @param height The height that the animation should be displayed as on the screen
     */
    public Animation createAnimation(String spriteSheet, int width, int height) {
        Animation a = new Animation(this.imageService, spriteSheet, width, height);
        a.init();
        return a;
    }
}
