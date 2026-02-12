package com.mrstride;

import java.io.File;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mrstride.entity.EntityFactory;
import com.mrstride.gui.MainFrame;
import com.mrstride.services.DataService;
import com.mrstride.services.ImageService;


/**
 * Hello world!
 *
 */
@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private ImageService imageService;

    @Autowired
    private EntityFactory entityFactory;

    @Autowired
    private DataService dataService;
    
    public static void main( String[] args ) {
        deleteLogs();
        // BUG: my first run said that I was headless, but with this print, it says false. Odd.
        // System.out.println("Before: Headless mode: " + java.awt.GraphicsEnvironment.isHeadless());
        // Alternatively, I can set the headless to be false and all is good.
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Hello World");

        // TODO: uncomment this code 

        // Invoke the UI Thread to create the Main frame. 
        // SwingUtilities.invokeLater(() -> {
            // For illustration purposes, we pass along the services
            // to MainFrame which passes them along to AnimationsDialog.
            // This is an outside-in pattern and illustrates a manual
            // version of IoC because Main is deciding the concrete implementations
            // of the services we are passing in. 
            // It is a bit arduous because we have to add extra arguments.
            // It illustrates DI via Constructor Injection.

            //MainFrame.theFrame = new MainFrame(dataService, imageService, entityFactory);
            //MainFrame.theFrame.createFrame();
        //});
    }
    
    private static void deleteLogs() {
        
        // I can't seem to get the Log4j to automatically delete the file, so do it manually.
        // Delete the log file if it exists
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        System.out.println(logDir.getAbsolutePath());
        File[] files = logDir.listFiles((dir, name) -> name.endsWith(".log"));
        for (File logFile : files) {
            if (logFile.exists()) {
                if (logFile.delete()) {
                    System.out.printf("Log file (%s) deleted successfully\n.", logFile.getName());
                } else {
                    System.out.printf("Failed to delete the log file: %s.\n", logFile.getAbsolutePath());
                }
            }
        }
    }
}
