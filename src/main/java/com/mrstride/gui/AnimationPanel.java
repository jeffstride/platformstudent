package com.mrstride.gui;

import javax.swing.JPanel;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;

public abstract class AnimationPanel extends JPanel {

    public static final int REPAINT_DELAY = 10;
	public static final int PHYSICS_DELAY = 10;
	public static long PHYSICS_SPEED = 5;

    private Thread sleepThread;
	private Timer physicsTimer;
	private Timer paintTimer;

    private volatile boolean done = false;

    public AnimationPanel() {        
        this.setBackground(Color.WHITE);
    }

    public abstract void update();

    public void start2() {
        // ScheduleAtFixedRate will be called even if the previous repaint() took to long.
        // But, since repaint() is just a request, this should be fine and good.
        /*
        paintTimer = new Timer();
        paintTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() { repaint(); } 
            }, 0, PHYSICS_DELAY);
        */

        // schedule will possibly delay after the completion of the previous task 
        physicsTimer = new Timer();
        physicsTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() { update(); repaint(); }
            }, 0, AnimationPanel.PHYSICS_DELAY);

    }

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
        if (physicsTimer != null) {
            physicsTimer.cancel();
            physicsTimer = null;
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
