package com.mrstride.services;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.stereotype.Service;

@Service
public class ImageServiceProvider implements ImageService {

    private Logger consoleLogger;

    /**
     * Maps unique image identifier to ImageInfoRecord
     */
    private Map<String, ImageInfoRecord> images = new HashMap<>();

    private Map<String, SpriteSheetInfo> sheets = new HashMap<>();

    // Need to use a "Constructor injection" to get the logging service
    // to be available at the time of the constructor.
    public ImageServiceProvider() {
        consoleLogger = LogManager.getLogger("console");
    }

    @Override
    public Set<String> getImages() {
        return images.keySet();
    }

    @Override
    public Set<String> getSpriteSheets() {
        return sheets.keySet();
    }

    @Override
    public BufferedImage getImage(String identifier) throws IOException {
        if (images.containsKey(identifier)) {
            ImageInfoRecord iir = images.get(identifier);
            if (iir.image == null) {
                // We need to load (or download) the image
                if (iir.isLocalFile()) {
                    loadImageFromFile(iir);
                    if (iir.needToConvertToTransparent()) {
                        iir.image = ImageService.convertToTransparentPNG(iir.image, 8);
                    }
                } else {
                    loadImageFromUrl(iir);
                }               
                return iir.image;
            } else {
                return iir.image;
            }
        }

        // the id wasn't found in our map
        consoleLogger.error("ERROR: Image identifier not found. Add it first.  {}", identifier);
        throw new UnexpectedException("Image identifier not found. Add it first. " + identifier);
    }

    /**
     * Image information is added for later storage and retrieval behavior
     *      
     * @param identifier A unique name for quick lookup and later retrieval
     * @param uri Could be "relativepath/to/image.jpg" or "https://a.b/image.jpg"
     * @param type Flags to determine if the image is cached locally, the resource directory,
     *        and whether it is a single image or a sprite sheet.
     * @return true if successful
     * @throws FileNotFoundException 
     */
    @Override
    public void addImageInfo(String id, String uri, int type) throws FileNotFoundException {
        if (images.containsKey(id)) {
            consoleLogger.error("ERROR: Image with id already added: {}", id);
            throw new IllegalArgumentException("Image with id already added: " + id);
        }
        ImageInfoRecord iir = new ImageInfoRecord(id, uri, type);
        // if the type indicates a local file, verify it exists
        if (iir.isLocalFile()) {
            // compose the absolutePath
            // Unix/Linix/Mac will be "/users/name/path/file.jpg"
            // Windows will be "C:/root/path/file.jpg"
            
            String path = iir.getAbsLocalPath();
            File file = new File(path);
            if (!file.exists()) {
                consoleLogger.error("ERROR: Image not found at: {}", file.getAbsolutePath());
                throw new FileNotFoundException("Image expected at: " + file.getAbsolutePath());
            }
            iir.uri = file.getAbsolutePath();
        } else {
            // Is there anything to do for a URL type? I think not.
        }
        images.put(id, iir);
    }

    public void addSheet(String id, SpriteSheetInfo ssi) {
        sheets.put(id, ssi);
    }

    public SpriteSheetInfo getSpriteSheetInfo(String id) {
        return sheets.get(id);
    }

    @Override
    public boolean deleteImage(String id) throws IOException {
        if (!images.containsKey(id)) {
            consoleLogger.error("ERROR: Image not added: {}", id);
            throw new IllegalArgumentException("Image not added: " + id);
        }
        ImageInfoRecord iir = images.get(id);

        File file = new File(iir.getAbsLocalPath());
        return file.delete();
    }

    @Override
    public boolean isAnimated(String id) {
        return sheets.containsKey(id);
    }

    private Image loadImageFromFile(ImageInfoRecord iir) throws IOException {
        File file = new File(iir.getAbsLocalPath());
        iir.image = ImageIO.read(file);
        return iir.image;
    }

    /**
     * Loads the image from the URL
     * @param iir
     * @return
     * @throws IOException
     */
    private Image loadImageFromUrl(ImageInfoRecord iir) throws IOException {
        if (iir.isLocalFile()) {
            consoleLogger.error("ERROR: Local file can't be downloaded: {}", iir.id);
            throw new UnexpectedException("Local file can't be downloaded.");
        }

        URI uri;
        try {
            uri = new URI(iir.uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            consoleLogger.error("ERROR: Invalid URI Syntax: {}", iir.uri);
            throw new IOException("Invalid URI Syntax: " + iir.uri);
        }
        URL url = uri.toURL();
        if (iir.saveLocally()) {
            // download and save locally in one step
            String destinationFile = iir.getAbsLocalPath();
            try (InputStream in = url.openStream();
                FileOutputStream out = new FileOutputStream(new File(destinationFile))) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            // Now that we have the file local, just load it
            return loadImageFromFile(iir);
        } else {
            // just download the image
            try (InputStream in = url.openStream()) {
                iir.image = ImageIO.read(in);
                return iir.image;
            }
        }
    }
    
}