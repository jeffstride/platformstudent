package com.mrstride.entity;

import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.mrstride.gui.Line;

/**
 * The MovingEntity is responsible for doing the following during **update**:  
 *   - moving the entity in the world 
 *   - Collision detection with floors, ceilings, walls 
 *   - Notifying derived classes of events:
 *      - onHitWall
 *      - onHitCeiling
 *      - onHitFloor
 *   - Allow derived classes to update their velocities according to User Events
 * 
 * The MovingEntity has a boundingRect and a nextBoundingRect.
 * 
 * The first thing to happen is that the nextBoundingRect is calculated to
 * where the entity would move if there was no collision.
 *  
 * Then all the floors, ceilings and walls are checked to see if we need
 * to react somehow. The reaction is to stop moving or bounce off the barrier
 * and to call the event onHitX to allow for extra reactions.
 * 
 */
public class MovingEntity extends Entity {
    protected int xVelocity;
    protected int yVelocity;
    private long timeLastMove;
    private long updateTime;

    // if we can move, we can fall. Are we on the ground?
    // just keep track of the last floor we were on!
    protected Line currentFloor;
    protected boolean canJump;

    /**
     * When "falling" down a slanted floor, it can become difficult to Jump because
     * the Entity can't jump while in the air off the floor. To account for this, we
     * keep track of how long the Entity has been falling (timeOffFloor) and allow
     * the Entity to jump if not much time has passed.
     * Yeah, this is kinda hacky.
     */
    private int timeOffFloor;

    /**
     * The rectangle that this entity wants to move to if there are no obstacles.
     */
    private Rectangle nextBoundingRect;

    public MovingEntity(String id, int x, int y, Map<String, Object> properties) {
        super(id, x, y, properties);
    }

    public MovingEntity(String id, int x, int y, int width, int height, Map<String, Object> properties) {
        super(id, x, y, width, height, properties);
    }

    @Override
    public void init() {
        super.init();
        physicsLogger.debug(String.format("Starting position is %s", boundingRect));
    }

    /**
     * This is called by the EntityManager at every Tick.
     * All entities are to update themselves.
     * 
     * The thread-safe way to add Entities during update is to first 
     * process all the Entities then add any spawned entities later.
     * A derived class that wants to add entities during update() would add them to
     * the thread-safe Queue provided here.
     * 
     * @param walls
     * @param floors
     * @param toAdd
     * @return True to keep the item in the list of entities. False to remove it.
     */    
    @Override
    public boolean update(List<Rectangle> walls, List<Line> floors, Queue<Entity> toAdd) {
        calcNextBoundingRect();

        // BEWARE: Check walls/ceilings before floors so that if we jump into a wall toward
        // a floor that we don't "land" on the floor on the other side, zero out velocities,
        // and then get push back outside of wall and basically hover.
        //
        // BEWARE: Incorrect ordering of can also cause the entity to get pulled up onto the 
        // floor and through the ceiling.
        checkWalls(walls);
        checkCeilings(floors);
        checkFloors(floors);
        move();

        return true;
    }

    /**
     * All collision detection and reactions to obstacles have already been done.
     * Now, the current MovingEntity is allowed to move to the proposed location
     * that is determined by the nextBoundingRect.
     */
    private void move() {
        // we should have already have had our next bounding rect checked and adjusted
        // Just move to our nextBoundingRect.
        timeLastMove = updateTime;
        boundingRect = new Rectangle(nextBoundingRect);
        x = boundingRect.x;
        y = boundingRect.y;
        physicsLogger.debug(String.format("Moving to %s", boundingRect));
    }

    /**
     * react to all ceilings
     * 
     * @param lines
     * @return true if we hit a ceiling
     */
    private boolean checkCeilings(List<Line> lines) {
        if (yVelocity < 0) {
            // we are moving up, jumping or flying. Check Ceilings
            for (Line line : lines) {
                if (reactToCeiling(line)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check all floors in the list to see if we landed on one or if we need
     * to start falling.
     * 
     * @param floors
     * @return if we hit a floor
     */
    private boolean checkFloors(List<Line> floors) {
        if (yVelocity < 0) {
            // if we were moving up, no need to check floors. Be done!
            canJump = false;
            currentFloor = null;
            timeOffFloor = 0;
            return false;
        }

        // delay resetting canJump to false to help us be more responsive
        if (timeOffFloor > 2) {
            // Going down a sloped floor can cause entity to be non-responsive to jumping.
            // Clear the canJump only after a few iterations.
            // canJump and currentFloor will set in the method reactToFloor
            physicsLogger.debug("Off Floor");
            this.canJump = false;
        }

        // we're not over/under a known floor
        this.currentFloor = null;

        for (Line floor : floors) {
            // We check all floors regardless because we might have floors making a V shape
            reactToFloor(floor);
        }

        if (this.currentFloor == null && canJump) {
            timeOffFloor++;
            physicsLogger.debug("Increment timeOffFloor {} (canJump set, but no current floor)", timeOffFloor);
        }

        return (currentFloor != null);
    }

    /**
     * Check every wall in the list to see how we react to it.
     * 
     * @param walls List of walls.
     */
    private void checkWalls(List<Rectangle> walls) {
        for (Rectangle wall : walls) {
            if (reactToWall(wall)) {
                break;
            }
        }
    }

    /**
     * This is called before we start a move and do all the collision
     * detection to see if we can move and how we move.
     * 
     * Calculate the nextBoundingRect.
     * Update the velocities of the current Entity.
     * Set the currentFloor that this Entity is on (or not).
     * 
     * @return Return the Rectangle that is our new, desired boundingRectangle
     */
    private void calcNextBoundingRect() {
        // allow derived classes to fall, or not
        updateVelocities();

        updateTime = System.currentTimeMillis();
        physicsLogger.debug("[StartMove]xVel: {}  yVel: {}  timeDiff: {}", xVelocity, yVelocity,
                (updateTime - timeLastMove));

        // Gets the Bounding HitBox for where the entity wants to move to next.
        int newX = (int) (x + xVelocity);
        int newY = (int) (y + yVelocity);
        nextBoundingRect = new Rectangle(newX, newY, boundingRect.width, boundingRect.height);
    }

    protected void updateVelocities() {
        // derived classes can override to enabling falling or other changes
        // due to keyboard input, etc.
    }

    /**
     * See if we hit the wall and react accordingly.
     * Stop moving and be outside the wall.
     * 
     * @param wall
     * @return true if we hit the wall
     */
    private boolean reactToWall(Rectangle wall) {

        // ***** STUDENT MUST IMPLEMENT THIS *****
        // TODO: Decide if we hit the wall
        // TODO: Log information
        // TODO: Update nextBoundingRect in onHitWall
        // TODO: call onHitWall
        return false;
    }

    /**
     * The MovingEntity has a default implementation when hitting a wall.
     * Derived classes may override to do additional work or alternative things.
     * @param wall The wall the Entity hit.
     */
    protected void onHitWall(Rectangle wall) {
        // ***** STUDENT MUST IMPLEMENT THIS *****
        // TODO: update velocities
        // TODO: Update nextBoundingRect (which is the next position)
        // TODO: Consider the case when the Hero is falling on top of a wall
        //       where the Hero could get moved to one side or the other.
    }

    /**
     * Helper to create a preferred display for a Rectangle2D.
     * 
     * @param rect the rectangle to display
     * @return display string
     */
    private static String rectString(Rectangle2D rect) {
        return String.format("x0:%d y0:%d x1:%d y2:%d",
                (int) rect.getX(), (int) rect.getY(), (int) rect.getMaxX(), (int) rect.getMaxY());
    }

    /**
     * See if we hit a ceiling and react properly.
     * 
     * @param line A line for the ceiling
     * @return true if Entity hit a ceiling and bounced down
     */
    private boolean reactToCeiling(Line line) {
        // ***** STUDENT MUST IMPLEMENT THIS *****
        // TODO: Decide if we hit the ceiling
        // TODO: Log information
        // TODO: Update nextBoundingRect in onHitCeiling
        // TODO: call onHitCeiling
        return false;
    }

    /**
     * When a MovingEntity hits a ceiling, this is called.
     * Derived classes may want to do more or less
     * 
     * @param line The ceiling we hit
     * @param rect The Union rectangle of boundingRect and nextBoundingRect
     */
    protected void onHitCeiling(Line line, Rectangle2D.Double rect) {
        // ***** STUDENT MUST IMPLEMENT THIS *****
        // TODO: Log information
        // TODO: Update nextBoundingRect (which is the next position)
        // TODO: velocities       
    }

    /**
     * 
     * @param floor A line represented using Rectangle fields
     * @return true if Entity is on this floor
     */
    private boolean reactToFloor(Line floor) {
        physicsLogger.debug("[reactToFloor]");
        // see if we are horizontally above/below the floor line
        if (yVelocity >= 0 && overUnderLine(floor)) {

            Rectangle2D.Double rect = Line.getUnionRect(boundingRect, nextBoundingRect);
            // BEWARE: not always on floor so we can't jump, so add +1 to the height of
            // bounding rect.
            // This also prevents us from restoring entity to above floor when walking
            // underneath it.
            rect.height++;
            boolean hit = floor.intersectsRect(rect);
            if (hit) {
                // notify any derived classes
                onHitFloor(floor, rect);
            }
            return currentFloor != null;
        }

        // keep looking at other floors
        return false;
    }

    /**
     * When a MovingEntity hits a floor, this is called.
     * Derived classes may want to do more or less
     * 
     * @param floor The floor we hit
     * @param rect The Union rectangle of boundingRect and nextBoundingRect
     */
    protected void onHitFloor(Line floor, Rectangle2D.Double rect) {
        physicsLogger.debug("  Hit! Union: {} floor {}", rectString(rect), floor);
        // boost the entity to be above the floor
        do {
            physicsLogger.debug("    pushing up 1");
            // push the rectangle up until it doesn't hit anymore
            rect.y -= 1;
        } while (floor.intersectsRect(rect));

        physicsLogger.debug("On Floor");
        canJump = true;
        timeOffFloor = 0;
        currentFloor = floor;

        // move the entity to be on this floor at floorY
        nextBoundingRect.y = (int) (rect.y + rect.height) - nextBoundingRect.height;
        physicsLogger.debug("    Pushed to: {} floor {}", rectString(nextBoundingRect), floor);

        // zero out vertical velocity
        yVelocity = 0;
    }

    /**
     * Returns true if the entity is directly over or under the line.
     * Returns false if the entity is completely to the left or right of the line.
     * 
     * @param line The Line to check
     * @return true if the entity is directly over or under the line.
     */
    private boolean overUnderLine(Line line) {
        int x1 = nextBoundingRect.x;
        int x2 = nextBoundingRect.x + nextBoundingRect.width;

        // if the left or right point is between the line bounds
        // then this entity is over/under the line
        return ((x1 >= line.x1 && x1 <= line.x2) || (x2 >= line.x1 && x2 <= line.x2));
    }
}
