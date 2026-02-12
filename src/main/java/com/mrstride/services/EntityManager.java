package com.mrstride.services;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JPanel;

import com.mrstride.entity.Entity;
import com.mrstride.entity.Hero;
import com.mrstride.gui.Line;


/**
 * This class does the following:
 * - Keeps tracks of all the Entities, floors and walls.
 *      - adds and removes them safely (when multi-threading is enabled)
 *      - provides access to the Hero
 * - Asks all the Entities to move themselves.
 * - Asks all the Entities to paint themselves.
 * 
 * It is important for this class to assure that Entity actions are
 * independent so that they can be parallelized. 
 * 
 * In the future it may also:
 * - Use parallelStreams to enable multithreading
 * - Keep track of Entities in viewable window to optimize painting
 */
public class EntityManager {
    private List<Line> floors;
    private List<Rectangle> walls;
    private Queue<Entity> entities; 

    private Logger logger = LogManager.getLogger("console");

    public EntityManager() {
        clear();
    }

    public void clear() {
        logger.debug("Clearing Entity Manager");

        // This should be overridden when entities are added.
        // Initialize to something so that we don't have to always
        // check if the hero is null or not.
        // Hero.hero will get reset when a new Hero is instantiated

        floors = new ArrayList<>();
        walls = new ArrayList<>();
        entities = new ConcurrentLinkedQueue<>(); // new ArrayList<>();
    }

    public void addFloor(Line floor) {
        floors.add(floor);
    }

    public void addWall(Rectangle wall) {
        walls.add(wall);
    }

    public void addEntity(Entity entity) {
        if (entity.isHero()) {
            logger.debug("Adding Hero");
        }
        entities.add(entity);
    }

    public int getFloorCount() {
        return floors.size();
    }

    public int getWallCount() {
        return walls.size();
    }

    public int getEntityCount() {
        return entities.size();
    }

    /**
     * Allow all the MovingEntities to add listener to a JPanel.
     * This should get called by the JPanel when it restarts with new entities.
     * 
     * @param panel The panel to listen to.
     */
    public void createEventHandlers(JPanel panel) {
        for (Entity entity : entities) {
            KeyListener listener = entity.getKeyListener();
            if (listener != null) {
                panel.addKeyListener(listener);
            }

            // TODO: add mouse listeners when we need them
        }
    }

    /**
     * This method gets called by the GamePanel::update() which is triggered by the
     * AnimationPanel's Thread. This method will move all the objects every
     * "tick".
     */
    public void moveAllObjects() {
       
        // Entities are prohibited (by convention) to remove themselves
        // from the list of entities. But an entity may want to add/remove 
        // entities to this. For Assignment-One this won't be done.
        // Laster, entities would indicate this action which would be done
        // after all entities are updated.
        // 
        
        for (Entity entity : entities) {
            entity.update(walls, floors);
        }

        // In the future, we'd add/remove entities here
        // In the future, we'd do collision detection here.
        
    }

    /**
     * This is on the GUI thread, triggered by a repaint() scheduled
     * by the paintTimer.
     * 
     * @param g       Graphics object to draw in
     * @param xOffset The amount to offset x-position
     * @param yOffset The amount to offset y-position
     */
    public void drawAllObjects(Graphics g, int xOffset, int yOffset) {
        // Note: If entities are added or removed from the list of
        // in another Thread then a for-each loop will throw a
        // ConcurrentModificationException.
        for (Entity entity : entities) {
            entity.draw(g, xOffset, yOffset);
        }

        drawFloorsAndWalls(g, xOffset, yOffset);
    }

    /**
     * Draws the floors and walls.
     * This does not need to be synchronized because it is private and calling
     * methods will be synchronized.
     * 
     * @param g       Graphics to draw in
     * @param xOffset The offset using the Hero (centered)
     * @param yOffset The offset using the Hero (centered)
     */
    private void drawFloorsAndWalls(Graphics g, int xOffset, int yOffset) {
        // Let's just draw the floors and walls

        Graphics2D g2d = (Graphics2D) g;

        // Set the line thickness
        float thickness = 2.0f;
        g2d.setStroke(new BasicStroke(thickness));

        // Set the floor color
        g2d.setColor(Color.BLUE);

        for (Line floor : floors) {
            g.drawLine((int) floor.x1 - xOffset, (int) floor.y1 - yOffset, (int) floor.x2 - xOffset,
                    (int) floor.y2 - yOffset);
        }

        // Set the wall color
        g2d.setColor(Color.LIGHT_GRAY);
        for (Rectangle wall : walls) {
            g.fillRect(wall.x - xOffset, wall.y - yOffset, wall.width, wall.height);
        }
    }

}
