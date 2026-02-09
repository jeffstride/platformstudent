package com.mrstride.services;

import java.awt.image.BufferedImage;
import java.util.function.Function;

public class Animation {
    /**
     * This tick keeps track of our animation speeds. The "drummer"
     * will call public void Animation.tick(). That will update
     * this static value.
     */
    private static long tick = 0;

    private final ImageService imageService;

    // Direction is the last bit
    // Facing left  = odd mode  (ones bit on)
    // Facing right = even mode (ones bit off)
    // Note that Mr. Stride thought having direction be the last bit was 
    // a good idea at one point, but now isn't so sure.
    // You, the student, can use Animation constants that suit you.
    public static final int MODE_STILL = 0;
    public static final int MODE_RUNNING = 2;
    public static final int MODE_JUMPING = 4;
    public static final int MODE_FLYING = 6;
    private static final int MAX_MODES = 7;

    public static final int FACING_RIGHT = 0;
    public static final int FACING_LEFT = 1;

    /**
     * Add more instance fields here as you need them
     */
    private String id;
    private int width;
    private int height;
    private int mode;

    /**
     * Create an animation object.
     * 
     * @param id The id of the spritesheet with the animation
     * @param width The display width (not frame width)
     * @param height The display height (not the frame height)
     */
    public Animation(ImageService imageService, String id, int width, int height) {
        this.imageService = imageService;
        this.width = width;
        this.height = height;
        this.id = id;
    }

    /**
     * All initialization for this Animation should happen here.
     * This will get called by the AnimationFactory.
     */
    public void init() {
        // This init method is not complete!!

        // allow us to be an empty sheet so that the client can increment Tick count
        // without having to have a full animation object.
        if (id == null) {
            return;
        }

        // TODO: Get the spritesheet from the image service, call a helper method
        // to parse the spritesheet and cache all the frames.        
    }    

    /**
     * Tick should be called at every time increment.
     * We keep the tick count as a static so that it needs to be tracked
     * only by the "global" animation process instead of by each object.
     * 
     * Make tick an instance method so that we can mock the object.
     * Have the GamePanel create one instance of an Animation object
     * to call tick() during update(). This will avoid having any client
     * code call: Animation.tick++; If we want to mock Animation then
     * Animation.tick may not be properly scoped and available.
     */
    public void tick() {
        Animation.tick++;
    }
    
    /**
     * Allow the client to get notification if an animation has ended.
     * Normally, the animation would go to Animation.MODE_STILL, but the
     * client can override via the listener.
     * 
     * @param listener The method to receive the notification.
     */
    public void addAnimationEndedListener(Function<Integer, Integer> listener) {
        // we don't have a list of listeners. Just one.
        // Save the listener: animationEnded = listener;
        // When an animation sequences ends, invoke it.
    }

    /**
     * Set the type of animation that should be drawn by this Animation.
     * The direction of the Entity needs to be set separately via setDirection.
     * 
     * @param mode One of the Animation.MODE_constants
     */
    public void setMode(int mode) {
        // TODO: When we change the animation mode, note the tick value so that
        // animation frames can be determined.
    }

    /**
     * Gets the current animation mode
     * 
     * @return one of the Animation MODES. Includes Animation.FACING_LEFT/RIGHT.
     */
    public int getMode() {
        return mode;
    }

    /**
     * Get the correct image to represent the sprite. 
     * Consider the mode, direction, ticks since last frame, and whether the
     * animation repeats when complete.
     * 
     * mode is set by client via setMode(). options are: MODE_STILL, MODE_RUNNING, etc. 
     * direction = FACING_LEFT, FACING_RIGHT
     * 
     * @param direction FACING_RIGHT == 0. FACING_LEFT == 1.
     * @return The buffered image to draw
     */
    public BufferedImage getCurrentFrame(int direction) {
        // TODO: Calculate the frame number by using the tick value.
        // If we are at the end of an animation, reset as necessary.
        // Return the cached image in the correct direction.
        // OPTIONAL:
        // Perhaps someone is listening for the end of an Animation sequence
        // and we want to notify the listener. Knowing the end of an Animation
        // sequence can allow the program to start a new animation or take
        // some other action.
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
