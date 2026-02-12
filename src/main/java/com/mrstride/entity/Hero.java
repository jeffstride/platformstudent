package com.mrstride.entity;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.mrstride.services.Animation;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

public class Hero extends FallingEntity {

    private static Hero hero = new Hero(null, 100, 100, null);

    public static final String SCORE = "score";
    public static final String HEALTH = "health";
    private static final int X_CENTER = 300;

    private boolean leftPressed;
    private boolean rightPressed;
    private boolean jump;

    private Logger actionsLogger;

    public Hero(String id, int x, int y, Map<String, Object> properties) {
        super(id, x, y, properties);
    }

    public Hero(String id, int x, int y, int width, int height, Map<String, Object> properties) {
        super(id, x, y, width, height, properties);
    }

    @Override
    public void init() {
        super.init();
        actionsLogger = LogManager.getLogger("UserActionFile");
                
        properties.putIfAbsent(HEALTH, 100);
        properties.putIfAbsent(SCORE, 0);
        
        // make this hero THE Hero
        Hero.hero = this;
    }

    public static Hero getHero() {
        return Hero.hero;
    }
    
    public void reduceHealth(int amt) {
        // Optional: Handle if hero dies, etc.
        // JSON default values are Longs, not Integers.
        int health = ((Long)getProperty(HEALTH)).intValue();
        health -= amt;
        setProperty(HEALTH, Long.valueOf(health));
    }

    @Override
    public boolean isHero() {
        return true;
    }

    /**
     * Update directional velocities based on keys being pressed.
     * Update animationMode based on being on the floor and/or keys pressed.
     */
    @Override
    public void updateVelocities() {
        // update our falling velocity
        super.updateVelocities();

        // respond to keyboard status to move left/right
        physicsLogger.debug("Hero updating velocities: {} floor", canJump ? "ON" : "Off");
        if (leftPressed && !rightPressed) {
            xVelocity = -4;
        } else if (!leftPressed && rightPressed) {
            xVelocity = 4;
        } else {
            xVelocity = 0;
        }

        if (canJump && jump) {
            consoleLogger.debug("jumping!");
            yVelocity = -15;
        } else if (jump) {
            consoleLogger.debug("not on floor. Can't jump.");
        } else if (canJump && yVelocity == 0) {
            // Entity is not jumping. We are MODE_STILL or MODE_RUNNING
            // TODO: update animation as necessary
        }
        jump = false;
    }

    /**
     * This is the Hero's Key Listener.
     * We create it as an Anonymous Inner Class using the KeyAdapter
     * so that we don't have to override every method.
     * Allowing a getter like this allows us to implement the Playback
     * feature more easily which will simulate events and trigger them
     * for this key listener.
     * 
     * @return KeyListener code
     */
    @Override
    public KeyListener getKeyListener() {
        
        KeyListener listener = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                // char ch = e.getKeyChar();
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_LEFT) {
                    leftPressed = true;
                    setDirection(Animation.FACING_LEFT);
                    actionsLogger.debug("Left Pressed");
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    rightPressed = true;
                    setDirection(Animation.FACING_RIGHT);
                    actionsLogger.debug("Right Pressed");
                } else if (keyCode == KeyEvent.VK_UP) {
                    actionsLogger.debug("Up Pressed");
                    // possible jump
                    consoleLogger.debug("Attempt to jump");
                    jump = true;
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    // possible fly down
                    actionsLogger.debug("Down Pressed");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                //char ch = e.getKeyChar();
                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_LEFT) {
                    leftPressed = false;
                    actionsLogger.debug("Left Released");
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    rightPressed = false;
                    actionsLogger.debug("Right Released");
                } else if (keyCode == KeyEvent.VK_UP) {
                    actionsLogger.debug("Up Released");
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    // stop jumping or flying down
                    actionsLogger.debug("Down Released");
                }
            }
        };

        return listener;
    }

    /**
     * getXOffset is what allows us to always position the Hero in the center
     * of the screen.
     * @return xOffset for the Hero
     */
    public int getXOffset() {
        return this.x - X_CENTER;
    }
}
