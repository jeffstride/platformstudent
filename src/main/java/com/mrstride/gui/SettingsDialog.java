package com.mrstride.gui;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * The Student will do the following things:
 * 
 * 1) Create an ImageService (and Test). Large Task.
 * 2) Use IoC to get the ImageService via getBean().
 * 3) Add needed images for this dialog to the image service
 * 4) Create this Dialog
 *     - Use LayoutManagers to resize
 *     - Save settings into a Model
 *     - Present a progress bar (artificially long)
 */
public class SettingsDialog extends JPanel {

    public SettingsDialog() {
        addComponents();
        // change this to some Layout Manager
        setLayout(null);
    }

    public void addComponents() {
        JButton btnOkay = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");
        add(btnOkay);
        add(btnCancel);
        btnOkay.setBounds(30, 10, 70, 25);
        btnCancel.setBounds(150, 10, 90, 25);

        // Add action listener to the buttons
        btnOkay.addActionListener(e -> onOK());
        btnCancel.addActionListener(e -> MainFrame.showPanel(MainFrame.GAME_PANEL));

        // Set the OK button as the default button
        MainFrame.theFrame.getRootPane().setDefaultButton(btnOkay);

        // The size of this dialog
        setSize(600, 600);
    }

    private void onOK() {
        // TODO: Save work
        MainFrame.showPanel(MainFrame.GAME_PANEL);
    }

}
