package com.mrstride.gui;
import javax.swing.*;

import com.mrstride.services.ImageService;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * This class provides no functionality. It is only sample code intended
 * to illustrate how one can manually resize and hide clusters of components
 * using nested JPanels and a resize listener.
 * 
 * We create three panels that are manually positioned in this, the top-level JPanel.
 * 1. JPanel with an image and a combobox below it. Uses a BoxLayout manager.
 * 2. JPanel with one large Text panel. Uses a BorderLayout manager.
 * 3. JPanel, always visible, with radio buttons. It is used to show/hide
 *    the other panels. Uses a BoxLayout manager.
 * 
 * The SampleLayout does NOT use a LayoutManager. This way we can manually resize
 * and position the contained JPanels.
 * Each contained JPanel uses a Layout Manager to position & size the components.
 * 
 * We have a listener to respond to resize events. 
 * Each panel gets a proportional size of the total width. 
 * When we hide/show a panel, we resize the remaining panels to consume all horizontal space.
 * 
 * We create the SampleLayout in two steps.
 * 1. Create the JPanels & components. Add them to "this", and set up listeners.
 * 2. Resize and position the JPanels and components.
 * 
 * When we receive resize events, we can easily resize and position. 
 */
public class SampleLayout extends JPanel {

    private JPanel imageColumn;
    private JPanel textColumn;
    private JPanel radioColumn;

    private ImageService imageService;
    
    public SampleLayout(ImageService imageService) {
        this.imageService = imageService;
        createComponents(MainFrame.theFrame);
        resizeColumns();
    }

    public void createComponents(JFrame frame) {
        // No Layout Manager. We manually position & resize everything.
        this.setLayout(null); 

        imageColumn = createImageColumn();
        textColumn = createTextColumn();
        radioColumn = createRadioColumn();

        this.add(imageColumn);
        this.add(textColumn);
        this.add(radioColumn);

        // let's listen to our parent frame
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeColumns();
            }
        });

        imageColumn.setVisible(true);
        textColumn.setVisible(true);
        radioColumn.setVisible(true);
        
        this.setVisible(false);
    }

    private JPanel createImageColumn() {
        // We use the BoxLayout to stack the components vertically.
        // The components will consume the entire width of the Panel.
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(300, 300));
        imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        try {
            BufferedImage img = imageService.getImage("penguin");
            imageLabel.setIcon(new ImageIcon(img));
            panel.add(imageLabel);
        } catch (IOException ex) {
            // ignore. Don't add the image.
        }

        JComboBox<String> comboBox = new JComboBox<>(new String[]{"Option 1", "Option 2", "Option 3"});
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(comboBox);

        return panel;
    }

    private JPanel createRadioColumn() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ButtonGroup group = new ButtonGroup();

        // The radio buttons differ in only two ways:
        // 1. The text, provided by a String array
        // 2. The boolean values when setting column visibility. Provided by bit mask.
        String[] btnText = { "Show neither", "Show image only", "Show text only",  "Show all"};

        for (int ibtn = 0; ibtn < btnText.length; ibtn++) {
            JRadioButton radioButton = new JRadioButton(btnText[ibtn]);
            radioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(radioButton);
            panel.add(radioButton);
            
            // The lambda expression cannot deal with the transient variable, ibtn.
            // Create a constant to allow the lambda to function correctly
            final int visibleFlags = ibtn;
            radioButton.addActionListener(e -> {
                // show the JPanels according to the bit mask. imageColumn is low order bit.
                // 0 == 00 => false false  
                // 1 == 01 => false true
                // 2 == 10 => true false
                // 3 == 11 => true true
                imageColumn.setVisible((visibleFlags & 1) == 1);
                textColumn.setVisible((visibleFlags & 2) == 2);
                resizeColumns();
            });
        }

        return panel;
    }

    private JPanel createTextColumn() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // By using the BorderLayout.CENTER, the component will always consume
        // the entire JPanel space.
        JTextPane textPane = new JTextPane();
        panel.add(textPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Resize the columns according to visibility and parent size.
     */
    private void resizeColumns() {
        // Count the number of visible columns by looking at the isVisible flag.
        int count = 1;
        if (imageColumn.isVisible()) {
            count++;
        }
        if (textColumn.isVisible()) {
            count++;
        }

        // Calculate the width of each column
        int width = MainFrame.theFrame.getWidth();
        int colWidth = width / count;

        // keep a running x-position value
        int x = 0;
        if (imageColumn.isVisible()) {
            imageColumn.setBounds(x, 0, colWidth, 300);
            x += colWidth;
        }
        if (textColumn.isVisible()) {
            textColumn.setBounds(x, 0, colWidth, 300);
            x += colWidth;
        }

        // The radioColumn is always visible
        radioColumn.setBounds(x, 0, colWidth, 300);

        // We don't need to call revalidate();
        // We don't need to call repaint() 
        // This is because each JPanel's Layout Manager is handling that for us.
    }

}
