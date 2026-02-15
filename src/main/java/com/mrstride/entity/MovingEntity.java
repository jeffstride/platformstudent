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
 *
 * COLLISION DETECTION ALGORITHM OVERVIEW:
 * 
 * Each update cycle follows these steps:
 * 1. Update velocities based on input/physics
 * 2. Calculate intended X movement (nextBoundingRect)
 * 3. Check horizontal collisions (walls)
 * 4. Calculate intended Y movement
 * 5. Check vertical collisions (floors/ceilings)
 * 6. Move to final validated position
 * 
 * KEY CONCEPTS:
 * - boundingRect: Current position
 * - nextBoundingRect: Desired position (validated by collision checks)
 * - currentFloor: The floor we're currently standing on (null if airborne)
 * - canJump: Whether jump input should be accepted
 * - Coyote time: Grace period after leaving a floor where jumps still work
 * 
 * COORDINATE SYSTEM:
 * - X increases rightward
 * - Y increases downward (screen coordinates)
 * - Positive yVelocity = falling
 * - Negative yVelocity = jumping/rising
 */
public class MovingEntity extends Entity {
    private static final int COYOTE_TIME = 2;
    private static final int MOVE_ERROR_COUNT = 20;

    protected int xVelocity;
    protected int yVelocity;

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
     * @param floors
     * @param toAdd
     * @return True to keep the item in the list of entities. False to remove it.
     */    
    @Override
    public boolean update(List<Line> floors, Queue<Entity> toAdd) {
        updateVelocities();
        physicsLogger.debug("[StartMove] ({}, {}) OnFloor: {}, xVel: {}  yVel: {}", x, y, (currentFloor != null ? currentFloor : "False"), xVelocity, yVelocity); 

        // Phase 1: Horizontal movement and collision
        boolean hitWall = false;
        calcNextBoundingRectX();
        if (xVelocity != 0) {
            hitWall = checkWalls(floors);
            // move our x-direction before checking our y-direction
            move();
        }
        
        // Phase 2: Vertical movement and collision

        // If we hit a wall, then don't worry about floors during this update.
        // Except, if we need to fall. Otherwise, entity will cling to wall in mid-air.
        if (!hitWall || (currentFloor == null && yVelocity > 0)) {
            calcNextBoundingRectY();
            if (yVelocity >= 0) {
                // The entity is potentially falling. 
                // React to floors and reset currentFloor.
                checkFloors(floors); 
            } else {
                // Moving up.
                canJump = false;
                timeOffFloor = 0;

                // BEWARE: We can jump up off a floor, hit a ceiling and land back on the floor
                // without recognizing that we are on the floor again. This can cause the entity
                // to react to the floor on the next update as a wall in a bizarre way.
                // To account for this, re-react to the currentFloor after reacting to ceilings.
                Line priorFloor = currentFloor;

                // React to ceilings
                checkCeilings(floors);

                currentFloor = null;

                // React to prior floor
                if (priorFloor != null) {
                    // this will reset the floor if necessary
                    reactToFloor(priorFloor);
                }
            }
        }

        // our nextBoundingRect is where we want to move to
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
                // don't hit the floor we are standing on
                if (line == currentFloor) {
                    continue;
                }
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
        // delay resetting canJump to false to help us be more responsive
        if (timeOffFloor > COYOTE_TIME) {
            // Going down a sloped floor can cause entity to be non-responsive to jumping.
            // Clear the canJump only after a few iterations.
            // canJump and currentFloor are set in the method reactToFloor.
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
     * Our floors can effectively act like walls.
     * 
     * @param walls List of walls.
     * @return true if we hit and reacted to a wall
     */
    private boolean checkWalls(List<Line> walls) {
        physicsLogger.debug("Checking Walls. Cur Floor: {}  Cur Rect: {}", (currentFloor != null), boundingRect);

        boolean hitWall = false;
        for (Line wall : walls) {
            if (wall == currentFloor) {
                // if we are on a floor, don't treat it like a wall
                continue;
            }
            // keep checking walls so that the entity will not intersect any
            hitWall |= reactToWall(wall);
        }

        // If we hit any wall, zero out our x-velocity.
        // BEWARE: Don't change x-velocity before checking all walls because each wall check
        // looks at the current x-velocity.
        if (hitWall) {
            this.xVelocity = 0;
        }
        return hitWall;
    }

    /**
     * This is called before we start a move and do all the collision
     * detection to see if we can move and how we move.
     * 
     * Calculate the nextBoundingRect when moving in x-direction only.
     * 
     * This may update the velocities of the current Entity.
     */
    private void calcNextBoundingRectX() {
        // Gets the Bounding HitBox for where the entity wants to move to next.
        int newX = (int) (x + xVelocity);
        nextBoundingRect = new Rectangle(newX, y, boundingRect.width, boundingRect.height);
    }

    /**
     * This is called after moving in the X-Direction when the Entity hit nothing.
     * Calculate the nextBoundingRect when moving in Y-direction only.
     * The current nextBoundRect already includes the x-direction move.
     */
    private void calcNextBoundingRectY() {
        // Gets the Bounding HitBox for where the entity wants to move to next.
        int newY = (int) (nextBoundingRect.y + yVelocity);
        nextBoundingRect = new Rectangle(nextBoundingRect.x, newY, boundingRect.width, boundingRect.height);
    }

    /**
     * Updates x & y velocities, but not the x or y positions.
     * 
     * Derived classes must override to enabling falling or other movement
     * due to keyboard input, etc.
     */
    protected void updateVelocities() {
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
     * 
     * @param floor A line represented using Rectangle fields
     * @return true if Entity is on this floor
     */
    private boolean reactToFloor(Line floor) {
        physicsLogger.debug("[reactToFloor] {}", floor);
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
        int moves = 0;
        physicsLogger.debug("  Hit floor! Union: {} floor {}", rectString(rect), floor);
        // boost the entity to be above the floor
        do {
            physicsLogger.debug("    pushing up 1");
            // push the rectangle up until it doesn't hit anymore
            rect.y -= 1;

            if (++moves > MOVE_ERROR_COUNT) {
                physicsLogger.error(" Hit floor error. Moved too many times.");
                break;
            }
        } while (floor.intersectsRect(rect));

        if (isWalkableSlope(floor, 45)) {
            physicsLogger.debug("  Setting currentFloor = {}", floor);
            canJump = true;
            timeOffFloor = 0;
            currentFloor = floor;

            // zero out vertical velocity
            yVelocity = 0;
        } else {
            // This will cause the entity to not stick to vertical walls
            yVelocity = 2;
        }

        // move the entity to be on this floor at floorY
        nextBoundingRect.y = (int) (rect.y + rect.height) - nextBoundingRect.height;
        physicsLogger.debug("    Pushed to: {} floor {}", rectString(nextBoundingRect), floor);
    }

    /**
     * Determines if a line is walkable based on its slope angle.
     * Vertical walls (90°) and steep slopes are not walkable.
     * 
     * @param line The line to check
     * @param maxWalkableAngle Maximum walkable angle in degrees (typically 45-50)
     * @return true if the slope can be walked on
     */
    private boolean isWalkableSlope(Line line, double maxWalkableAngle) {
        double dx = line.x2 - line.x1;
        double dy = line.y2 - line.y1;
        
        // Calculate angle from horizontal (in degrees)
        double angleRadians = Math.atan2(Math.abs(dy), Math.abs(dx));
        double angleDegrees = Math.toDegrees(angleRadians);
        
        return angleDegrees <= maxWalkableAngle;
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

        int bMin = (int) Math.min(line.x1, line.x2);
        int bMax = (int) Math.max(line.x1, line.x2);
        return (x2 >= bMin) && (bMax >= x1);
    }

    /**
     * See if we hit the wall due to moving left or right. 
     * When hitting walls, there is no logic necessary for moving vertically.
     * 
     * Don't forget to log to physicsLogger.debug() for debugging purposes.
     * 
     * @param wall The Line of the wall to check
     * @return true if we hit the wall
     */
    private boolean reactToWall(Line wall) {
        
        boolean hit = wall.intersectsRect(nextBoundingRect);
        if (hit) {
            onHitWall(wall);
        } else {
            physicsLogger.debug("   No Wall: Next: {}  wall {}", rectString(nextBoundingRect), wall);
        }
        return hit;
    }

    /**
     * Check if we hit this ceiling. If we do, go below it.
     *
     * @param line The ceiling
     * @return true if Entity hit a ceiling and bounced down
     */
    private boolean reactToCeiling(Line line) {
        // no need to check ceilings unless we are going upwards
        // and the ceiling line is close to us.
        boolean check = yVelocity < 0 && overUnderLine(line);
        physicsLogger.debug("[reactToCeiling] {}", check ? "check" : "skip");

        if (check) {
            // BEWARE: Moving and jumping off a floor can cause us to initially intersect
            // the floor we are jumping off of. This Line should not be the currentFloor.
            boolean hit = line.intersectsRect(nextBoundingRect);
            if (hit) {
                onHitCeiling(line);
            }
            // stop looking if we hit a ceiling
            return hit;
        }

        // we need to look at other ceilings
        return false;
    }

    /**
     *       ******** STUDENT MUST IMPLEMENT THE BELOW METHODS ********
     */


    /**
     * This Entity hit a wall. Normal moving entities will move away from the wall.
     * Derived classes may react differently to hitting a wall. They may remove
     * themselves or do something different.
     *
     * Implementation "recipe":
     *   while we are hitting the wall:
     *       move away from the wall
     *       change union rect and nextBoundingRect
     * 
     * Don't forget to log to physicsLogger.debug() for debugging purposes.
     * 
     * @param wall The wall the Entity hit.
     */
    protected void onHitWall(Line wall) {
        // STUDENT MUST IMPLEMENT
    }

    /**
     * When a MovingEntity hits a ceiling, this is called.
     * Derived classes may want to do more or less
     * 
     * Implementation "recipe":
     *   while we are hitting the ceiling:
     *       move down 
     *       change union rect and nextBoundingRect
     *   update yVelocity
     * 
     * Don't forget to log to physicsLogger.debug() for debugging purposes.
     * 
     * @param line The ceiling we hit
     */
    protected void onHitCeiling(Line line) {
        // STUDENT MUST IMPLEMENT
    }
}
