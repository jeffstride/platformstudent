package com.mrstride.gui;

import javax.swing.*;
import org.apache.logging.log4j.Logger;

// This class starts all the threads and creates all the panels. It also creates the menu options.
public class MainFrame extends JFrame {

    public static final int GAME_PANEL = 0;
    public static final int SETTINGS_PANEL = 1;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;

    public static MainFrame theFrame = null;

    public static Logger physicsLogger;
    public static Logger consoleLogger;
    public static Logger actionsLogger;
    public static Logger perfLogger;

    // Our application may have many animated panels
    // But only one panel will be currently visible at a time
    private JPanel[] panels;
    private int currentPanel = -1;
    private GamePanel gamePanel;
    private SettingsDialog settingsPanel;

    public static void startGUI() throws InterruptedException {
        MainFrame.theFrame = new MainFrame();

        SwingUtilities.invokeLater(() -> theFrame.createFrame(theFrame));

        // only needed if we do work after the frame is created
        synchronized (theFrame) {
            theFrame.wait();
        }

    }

    /**
     * Create the main JFrame and all animation JPanels.
     * 
     * @param semaphore The object to notify when complete
     */
    private void createFrame(Object semaphore) {
        addMenuBar();
        panels = new JPanel[2];

        this.gamePanel = new GamePanel();
        this.settingsPanel = new SettingsDialog();

        panels[GAME_PANEL] = gamePanel;
        panels[SETTINGS_PANEL] = settingsPanel;

        for (JPanel panel : panels) {
            panel.setBounds(0, 0, MainFrame.WIDTH, MainFrame.HEIGHT);
            this.add(panel);
            panel.setVisible(false);
        }

        this.setSize(WIDTH, HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.currentPanel = -1;
        showPanel(GAME_PANEL);

        System.out.println("All done creating our frame");
        this.setVisible(true);
        
        synchronized (semaphore) {
            semaphore.notify();
        }
    }

    public static void showPanel(int index) {
        MainFrame.theFrame.showPanelInternal(index);
    }

    private void showPanelInternal(int index) {
        System.out.printf("Show Panel. Thread is: %s\n", Thread.currentThread().getName());

        // hide the current panel
        if (currentPanel != -1) {
            panels[currentPanel].setVisible(false);
            if (panels[currentPanel] instanceof AnimationPanel) {
                AnimationPanel ap = (AnimationPanel) panels[currentPanel];
                ap.stop();
            }
        }

        // show the correct panel
        currentPanel = index;
        panels[currentPanel].setVisible(true);
        panels[currentPanel].setFocusable(true);
        panels[currentPanel].setRequestFocusEnabled(true);
        panels[currentPanel].requestFocus();
        if (panels[currentPanel] instanceof AnimationPanel) {
            AnimationPanel ap = (AnimationPanel) panels[currentPanel];
            ap.start();
        }
    }

    /**
     * Add some menu options to control the experience.
     */
    private void addMenuBar() {

        JMenuBar bar = new JMenuBar();
        this.setJMenuBar(bar);

        JMenu menu = createAnimationMenu();
        bar.add(menu);
    }

    /**
     * Create the top-level menu that controls Animation
     * 
     * @return The JMenu object with all the JMenuItems in it.
     */
    private JMenu createAnimationMenu() {
        JMenu menu = new JMenu("Options");
        menu.setMnemonic('O');

        JMenuItem item = new JMenuItem("Restart", 'B');
        item.addActionListener(e -> {
            showPanel(GAME_PANEL);
            gamePanel.restart();
        });
        menu.add(item);

        item = new JMenuItem("Settings...", 'S');
        item.addActionListener(e -> showPanel(1));
        menu.add(item);
        
        return menu;
    }


}
