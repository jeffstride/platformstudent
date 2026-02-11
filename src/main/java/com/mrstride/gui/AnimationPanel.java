package com.mrstride.gui;

import javax.swing.JPanel;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;

public abstract class AnimationPanel extends JPanel {

	public static final int PHYSICS_DELAY = 10;

    private Thread sleepThread;
	private Timer paintTimer;

    private volatile boolean done = false;

    public AnimationPanel() {        
        this.setBackground(Color.WHITE);
    }

    public abstract void update();

    public void start() {
        done = false;

        // create a thread that sleeps between calling update/paint.
        this.sleepThread = new Thread(() -> {
            try {
                while (!done) {
                    update();
                    repaint();
                    Thread.sleep(AnimationPanel.PHYSICS_DELAY);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }    
        });
        this.sleepThread.start();
    }

    public void stop() {
        if (paintTimer != null) {
            paintTimer.cancel();
            paintTimer = null;
        }
        if (sleepThread != null) {
            done = true;
            try {
                sleepThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
