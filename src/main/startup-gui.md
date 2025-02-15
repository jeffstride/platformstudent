# Startup with GUI

## Lesson on JFrame and JPanel

## Create the JFrame
1. Implement JFrame::createFrame
```java
        this.setSize(WIDTH, HEIGHT);
        this.setTitle("CSS490 Platform");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // then
        this.setVisible(true);
```
2. Create the MainFrame on the GUI Thread
```java
    // This belongs in Main::main
    SwingUtilities.invokeLater(() -> {
        MainFrame.theFrame = new MainFrame();
        MainFrame.theFrame.createFrame();
    });
```

3. Create Options Menu Items
```java
    JMenu menu = new JMenu("Options");
    menu.setMnemonic('O');

    JMenuItem item = new JMenuItem("Restart", 'B');
    item.addActionListener(e -> {
        showPanel(GAME_PANEL);
        ((GamePanel) panels[GAME_PANEL]).restart();
    });
    menu.add(item);

    item = new JMenuItem("Settings...", 'S');
    item.addActionListener(e -> showPanel(1));
    menu.add(item);
    
    return menu;
```
