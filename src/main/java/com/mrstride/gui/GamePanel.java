package com.mrstride.gui;

import com.mrstride.Main;
import com.mrstride.entity.Hero;
import com.mrstride.services.DataService;
import com.mrstride.services.EntityManager;

import java.awt.Graphics;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class GamePanel extends AnimationPanel {
    // These should be Constructor Injected into the GamePanel
    private EntityManager entityManager;
    private DataService loader;
    private Logger actionsLogger;
    private Logger perfLogger;
    public GamePanel() {
        // Use Dependency Injection to get the loader object
        //loader = new HardCodedData();
        this.actionsLogger = LogManager.getLogger("UserActionFile");
        this.perfLogger = LogManager.getLogger("PerformanceFile");
        restart();
    }

    public void restart() {
        actionsLogger.info("Restart");
        // recreate our entities and initialize everything
        //entityManager = loader.loadLevel(1);
        // With all new entities, we need to create the event handlers
        createEventHandlers();
    }

    /**
     * update() gets called by the AnimationPanel timer/thread.
     */
    public void update() {
        // every time we update, we need to keep track of our ticks for Animation
        // TODO: get a tickTracker Animation object to invoke tick()
        // tickTracker.tick();

        long startTime = System.currentTimeMillis();
        // do the physics for the animations
        //entityManager.moveAllObjects();
        long stopTime = System.currentTimeMillis();
        perfLogger.debug("Updated Time: {}", (stopTime-startTime));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        long startTime = System.currentTimeMillis();

        // get our offset
        int xOffset = Hero.getHero().getXOffset();
        // tell all our entities to paint
        entityManager.drawAllObjects(g, xOffset, 0);

        long stopTime = System.currentTimeMillis();
        perfLogger.debug("Paint Time: {}", (stopTime-startTime));
    }

    private void createEventHandlers() {
        // entityManager.createEventHandlers(this);
        // TODO: Add mouse listener and add key listener.
    }

    /*
    private void onMouseClicked(MouseEvent me) {
        int x = me.getX();
        int y = me.getY();
        Main.actionsLogger.info("Clicked at ({}, {})", x, y);
    }
    */

}
