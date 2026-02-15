package com.mrstride.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.mrstride.gui.Line;
import com.mrstride.services.Animation;
import com.mrstride.services.AnimationFactory;
import com.mrstride.services.ImageService;
import com.mrstride.services.SpriteSheetInfo;

/**
 * This base Entity does not move. It does not fall. It does not collide with anything.
 * All Entity objects must be created with the EntityFactory to assure that @Autowired
 * is honored.
 */
public class Entity  {
    
    // property that says this entity can hit the Hero
    public static final String IS_HIT = "isHit";

    /**
     * The x, y, width, height coordinates should be easily accessibly by derived classes.
     * Make them protected instead of private.
     */
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    private boolean isAnimated;
    private Animation animation;

    // determines whether to resize according to the original size of the image
    private boolean useImageSize;

    // image id of the entity
    private String id;

    private AnimationFactory aniFactory;
    private ImageService imageService;
    protected EntityFactory entityFactory;

    // This will get initialized in the init() method.
    protected Logger physicsLogger;
    protected Logger consoleLogger;

    /**
     * One of:
     *  Animation.FACING_RIGHT = 0;
     *  Animation.FACING_LEFT = 1
     */
    private int direction = Animation.FACING_RIGHT;

    /**
     * This is the bounding rectangle of this Entity. It is used for hit detection.
     */
    protected Rectangle boundingRect;

    /**
     * These are the images for the non-animated sprite that will be drawn
     * depending on which direction the entity is facing.
     */
    private BufferedImage spriteRight = null;
    private BufferedImage spriteLeft= null;

    /**
     * Keep a map of extended properties for this Entity.
     */
    protected Map<String, Object> properties;


    /**
     * Create an entity with an image and set size. If the image is null then
     * the Entity is drawn as a grey box.
     * 
     * @param id The id of the image (the key)
     * @param x The starting x-position of this entity
     * @param y The starting y-position of this entity
     * @param width Set the entity to this width
     * @param height Set the entity to this height.
     */    
    public Entity(String id, int x, int y, int width, int height, Map<String, Object> properties) {
        internalInit(id, x, y, width, height, properties);

        // Load the image but keep our set width/height
        this.useImageSize = false;
    }

    /**
     * Create an entity using the image's information to set the width & height
     * @param id The id of the image (the key)
     * @param x The starting x-position of this entity
     * @param y The starting y-position of this entity
     */
    public Entity(String id, int x, int y, Map<String, Object> properties) {
        internalInit(id, x, y, 0, 0, properties);

        // Load the image and set our width/height to be the image's size
        this.useImageSize = true;
    }

    /**
     * Create an entity initially without an image. Establish all the fundamentals.
     * Some values may be overridden later.
     * 
     * @param x The starting x-position of this entity
     * @param y The starting y-position of this entity
     * @param width Set the entity to this width
     * @param height Set the entity to this height.
     * @param properties The set of properties to as the initial set
     */
    private void internalInit(String id, int x, int y, int width, int height, Map<String, Object> properties) {
        this.id = id;
        spriteRight = null;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        boundingRect = new Rectangle(x, y, Math.max(1, width), Math.max(1, height));
        this.properties = properties == null ? new HashMap<>() : new HashMap<>(properties);
    }

    // This allows us to manually inject services while not complicating the constructors.
    // These need to be set before we call init().
    // This follows the Setter Injection style, but is not @Autowired
    public void setServices(AnimationFactory factory, ImageService imgService, EntityFactory entityFactory) {
        this.aniFactory = factory;
        this.imageService = imgService;
        this.entityFactory = entityFactory;
    }

    /** 
     * The initialization process will require Autowired components/services.
     * The Entity needs to be autowired BEFORE we call init.
     */ 
    public void init() {
        physicsLogger = LogManager.getLogger("PhysicsFile");
        consoleLogger = LogManager.getLogger("console");
        
        loadEntityImages(id, useImageSize);
    }


    /**
     * This is called to update (move) an Entity.
     * 
     * This is also the way to add Entities during update that should enable
     * us to use parallelStream to process all the Entities.
     * A derived class that wants to add entities during update() would add them to
     * the thread-safe Queue provided here.
     * 
     * @param floors
     * @param toAdd
     * @return True to keep this Entity in the list of entities. False to remove it.
     */
    public boolean update(List<Line> floors, Queue<Entity> toAdd) {

        return true;
    }

    public Rectangle getBoundingRect() {
        return boundingRect;
    }


    /**
     * Allows us to hook up listeners for Entities by getting them from the Entity.
     * The Hero will override.
     * 
     * @return The KeyListener
     */
    public KeyListener getKeyListener() {
        return null;
    }
    
    public boolean isHero() {
        return false;
    }

    public boolean isHitEntity() {
        return properties.containsKey(Entity.IS_HIT);
    }

    public int getIntPropertySafely(String name) {
        Object obj = properties.get(name);
        if (obj == null || !(obj instanceof Long)) {
            return 0;
        }
        return ((Long) obj).intValue();
    }    

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public Object setProperty(String name, Object value) {
        return properties.put(name, value);
    }

    /**
     * Allow derived classes to get notifications if it hit the Hero.
     * EntityManager calls this so we need to make it public.
     * @param hero
     */
    public void onHitHero(Hero hero) {

    }

    /**
     * Load the image for this Entity.
     * If we have Animation, then set that up, too.
     * The image will be resized and cached for left and right facing directions.
     * 
     * @param id image id used to load the image
     * @param useImageSize true = use the size of the image for this Entity's width/height.
     *                     false = use the size as already set in the constructor.
     */
    private void loadEntityImages(String id, boolean useImageSize) {
        // TODO: Use the image service to load the BufferedImage for this Entity
        if (id != null && id.length() > 0) {
            try {
                // see if our image id is an animated spritesheet
                isAnimated = imageService.isAnimated(id);
                if (isAnimated) {
                    // we are animated. Get our animation object.
                    SpriteSheetInfo ssi = imageService.getSpriteSheetInfo(id);

                    if (useImageSize) {
                        width = ssi.size()[0];
                        height = ssi.size()[1];
                    }

                    // get our animation initialized with our images and size
                    animation = aniFactory.createAnimation(id, width, height);
                } else {
                    spriteRight = (BufferedImage) imageService.getImage(id);
                    // set width/height according to image size
                    if (useImageSize) {
                        width = spriteRight.getWidth();
                        height = spriteRight.getHeight();
                    } else {
                        // resize our image
                        spriteRight = ImageService.resize(spriteRight, width, height);
                    }
                    // Get our left-facing image
                    spriteLeft = ImageService.flipHorizontally(spriteRight);
                }
            } catch (IOException e) {
                spriteRight = null;
                e.printStackTrace();
            }
        }        

        // assure that we have our bounding rectangle set
        boundingRect = new Rectangle(x, y, width, height);
    }

    /**
     * This draws the entity into the Graphics using the provided offsets.
     * Derived classes may override to draw as they need to.
     * If This entity is Animated, then animation will happen using the Animation
     * service.
     * 
     * @param g Graphics to draw into
     * @param xOffset subtract this from the entity's x-position
     * @param yOffset subtract this from the entity's y-position
     */
    public void draw(Graphics g, int xOffset, int yOffset) {
        // When Animation is added, the image will be retrieved from that Animation object.
        if (spriteRight == null) {
            g.setColor(Color.GRAY);
            g.fillRect(x - xOffset, y - yOffset, width, height);
        } else {
            BufferedImage sprite = spriteRight;
            if (getDirection() == Animation.FACING_LEFT) {
                sprite = spriteLeft;
            }
            g.drawImage(sprite, x - xOffset, y - yOffset, null);
        }
    }

    /**
     * Derived classes can override this to show the correct direction of the sprite.
     * Otherwise, we use the value provided in setAnimationMode.
     * 
     * @return Animation.FACING_LEFT or FACING_RIGHT
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Keep track of direction, Left or Right.
     * Pass along to the Animation object.
     * @param direction
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * Derived classes would call this to put the sprite into the correct animation mode.
     *     Animation.MODE_STILL = 0;
     *     Animation.MODE_RUNNING = 2;
     *     Animation.MODE_JUMPING = 4;
     *     Animation.MODE_OTHER = 6;
     */
    public void setAnimationMode(int mode) {
        // TODO: Pass the mode to the animation object
    }
    
    public BufferedImage getSpriteLeft() {
        return this.spriteLeft;
    }
    
    public BufferedImage getSpriteRight() {
        return this.spriteRight;
    }
}
