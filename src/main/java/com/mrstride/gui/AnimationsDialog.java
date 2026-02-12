package com.mrstride.gui;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * The Student will do the following things:
 * 
 * 1) Use Constructor Injection to get dependencies.
 * 2) Display a dialog that allows the user to view all the images available.
 * 
 * To help, look at the Sample Layout to see how to place components,
 * and how to show/hide components.  
 * 
 * The Dialog should eventually allow one to see all the sprite sheets,
 * resize images, and watch animations at various speeds.  
 * 
 * Students should use services as much as possible in this implementation.  
 */
public class AnimationsDialog extends JPanel {

    public AnimationsDialog() {
        // change this to some Layout Manager
        setLayout(null);

        addComponents();
    }

    public void addComponents() {
        JButton btnOkay = new JButton("OK");

        add(btnOkay);

        btnOkay.setBounds(30, 10, 70, 25);

        // Add action listener to the buttons
        btnOkay.addActionListener(e -> onOK());

        // Set the OK button as the default button
        MainFrame.theFrame.getRootPane().setDefaultButton(btnOkay);

        // The size of this dialog
        setSize(600, 600);
    }

    private void onOK() {
        MainFrame.showPanel(MainFrame.GAME_PANEL);
    }
}
