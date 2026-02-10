package com.mrstride.entity;

import java.util.Map;

public class FallingEntity extends MovingEntity {

    public static int GRAVITY = 1;
    public static final int CRITICAL_VELOCITY = 20;

    public FallingEntity(String id, int x, int y, Map<String, Object> properties) {
        super(id, x, y, properties);
    }

    public FallingEntity(String id, int x, int y, int width, int height, Map<String, Object> properties) {
        super(id, x, y, width, height, properties);
    }

    @Override
    protected void updateVelocities() {
        // increment our yVelocity by gravity if allowed (not on floor. not flying)

        // Do NOT use !canJump because we delay the canJump setting to make the entity
        // more responsive to user command to jump as "falling" down a sloped floor.
        // The entity needs to fall to the floor. Only if the entity is falling for
        // more than 2 ticks does canJump become false.
        if (currentFloor == null) {
            yVelocity += FallingEntity.GRAVITY;
            
            // don't allow entities to fall too quickly
            if (yVelocity > CRITICAL_VELOCITY) {
                yVelocity = CRITICAL_VELOCITY;
            }
        }
    }
}