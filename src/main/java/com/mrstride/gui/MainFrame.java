package com.mrstride.gui;

import javax.swing.*;

// This class starts all the threads and creates all the panels. It also creates the menu options.
public class MainFrame extends JFrame {

    public static final int GAME_PANEL = 0;
    public static final int ANIIMATIONS_PANEL = 1;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;

    public static MainFrame theFrame = null;

    // Our application may have many animated panels
    // But only one panel will be currently visible at a time
    private JPanel[] panels;
    private int currentPanel = -1;

    /**
     * Create the main JFrame and all animation JPanels.
     */
    public void createFrame() {
        addMenuBar();
        panels = new JPanel[2];

        panels[GAME_PANEL] = new GamePanel();
        panels[ANIIMATIONS_PANEL] = new AnimationsDialog();

        for (JPanel panel : panels) {
            panel.setBounds(0, 0, MainFrame.WIDTH, MainFrame.HEIGHT);
            this.add(panel);
            panel.setVisible(false);
        }
        // TODO: set size, title, and close operation

        this.currentPanel = -1;
        showPanel(GAME_PANEL);

        System.out.println("All done creating our frame");

        // TODO: JFrame must be set to visible 
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

        JMenu menu = createMainMenu();
        bar.add(menu);

        menu = createConsoleMenu();
        bar.add(menu);
    }

    /**
     * Create the top-level menu for Options
     * 
     * @return The JMenu object with all the JMenuItems in it.
     */
    private JMenu createMainMenu() {
        JMenu menu = new JMenu("Options");
        menu.setMnemonic('O');

        // TODO: Create menu items
        
        return menu;
    }
    /**
     * Create the top-level menu for Console work
     * 
     * @return The JMenu object with all the JMenuItems in it.
     */
    private JMenu createConsoleMenu() {
        JMenu menu = new JMenu("Console");
        menu.setMnemonic('C');

        // TODO: create menu items
        return menu;
    }
}
