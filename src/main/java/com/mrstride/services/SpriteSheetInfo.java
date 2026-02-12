package com.mrstride.services;

/*
 * A record is immutable.
 * It will have implicit getters for each "field". For example:
 *    public int[] size() { return this.size; }
 * 
 * We have 4 basic modes, each with left-right facing.
 * 
 * Modes in Animation class:
 *   public static final int MODE_STILL = 0;
 *   public static final int MODE_RUNNING = 1;
 *   public static final int MODE_JUMPING = 2;
 *   public static final int MODE_OTHER = 3;
 *   private static final int MAX_MODE_INDEX = 3;
 */
public record SpriteSheetInfo(
    String imageId,         // the name of the ID in the image service
    int[] size,             // { width, height } of each frame in the spriteSheet
    boolean[] repeat,       // { repeat frames continuously } false = stop animation & go to MODE_STILL      
    int[] frames,           // { count of frames in each mode }
    int[] animationSpeed,   // { count of ticks before moving to next frame for each mode }
    int[][] modeOrigins,    // { {x, y} ... } of origin for a mode. null if mode not supported
    int[][] rects)          // { {x, y, width, height} } specific position of usable image in frame
    { }
