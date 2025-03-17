package com.mrstride.gui;

import com.mrstride.Main;

import java.awt.Graphics;

public class GamePanel extends AnimationPanel {

    //private EntityManager entityManager;
    //private DataService loader;

    public GamePanel() {
        // Use Dependency Injection to get the loader object
        //loader = new HardCodedData();
        restart();
    }

    public void restart() {
        Main.actionsLogger.info("Restart");
        // recreate our entities and initialize everything
        //entityManager = loader.loadLevel(0);
        createEventHandlers();
    }

    public void update() {
        long startTime = System.currentTimeMillis();
        // do the physics for the animations
        //entityManager.moveAllObjects();
        long stopTime = System.currentTimeMillis();
        Main.perfLogger.debug("Updated Time: {}", (stopTime-startTime));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        long startTime = System.currentTimeMillis();

        // get our offset
        // int xOffset = entityManager.getXOffset();
        // tell all our entities to paint
        // entityManager.drawAllObjects(g, xOffset, 0);

        long stopTime = System.currentTimeMillis();
        Main.perfLogger.debug("Paint Time: {}", (stopTime-startTime));
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
