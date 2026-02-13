package com.mrstride.gui;

import javax.swing.*;

import com.mrstride.entity.EntityFactory;
import com.mrstride.services.DataService;
import com.mrstride.services.ImageService;

// This class starts all the threads and creates all the panels. It also creates the menu options.
public class MainFrame extends JFrame {

    public static final int GAME_PANEL = 0;
    public static final int ANIMATIONS_PANEL = 1;
    public static final int SAMPLE_PANEL = 2;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;

    public static MainFrame theFrame = null;

    private final DataService dataService;
    private final ImageService imageService;
    private final EntityFactory entityFactory;

    // Our application may have many animated panels
    // But only one panel will be currently visible at a time
    private JPanel[] panels;
    private int currentPanel = -1;

    public MainFrame(DataService dataService, ImageService imageService, EntityFactory entityFactory) {
        this.dataService = dataService;
        this.imageService = imageService;
        this.entityFactory = entityFactory;
    }
    /**
     * Create the main JFrame and all animation JPanels.
     */
    public void createFrame() {
        addMenuBar();
        panels = new JPanel[3];

        panels[GAME_PANEL] = new GamePanel();
        panels[ANIMATIONS_PANEL] = new AnimationsDialog();
        panels[SAMPLE_PANEL] = new SampleLayout(imageService);

        for (JPanel panel : panels) {
            panel.setBounds(0, 0, MainFrame.WIDTH, MainFrame.HEIGHT);
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
            this.remove(panels[currentPanel]);
            panels[currentPanel].setVisible(false);
            if (panels[currentPanel] instanceof AnimationPanel) {
                AnimationPanel ap = (AnimationPanel) panels[currentPanel];
                ap.stop();
            }
        }

        // show the correct panel
        currentPanel = index;
        this.add(panels[currentPanel]);
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

        menu = createDemoMenu();
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
        // Restart
        // Animations
        // Save playback...
        // Playback...

        
        return menu;
    }
    /**
     * Create the top-level menu for Console work
     * 
     * @return The JMenu object with all the JMenuItems in it.
     */
    private JMenu createDemoMenu() {
        JMenu menu = new JMenu("Demo");
        menu.setMnemonic('D');

        // TODO: create menu items
        JMenuItem item = new JMenuItem("Sample Layout");
        item.addActionListener(e -> showPanel(SAMPLE_PANEL));
        menu.add(item);

        return menu;
    }
}
