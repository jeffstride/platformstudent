package com.mrstride.services;

import java.io.IOException;
import java.rmi.UnexpectedException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
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

    /**
     * Gets the image that maps to the identifier.
     * 
     * Once the Image is retrieved from the file, the image should be cached.
     * 
     * @param identifier The name of the image information
     * @return The BufferedImage of the image  
     * @throws IOException if the file for the image is bad (e.g. not found)
     */
    @Override
    public BufferedImage getImage(String identifier) throws IOException {
        if (images.containsKey(identifier)) {
            ImageInfoRecord iir = images.get(identifier);
            if (iir.image == null) {
                // We need to load (or download) the image
                if (iir.isLocalFile()) {
                    loadImageFromFile(iir);
                    if (iir.needToConvertToTransparent()) {
                        File origFile = new File(iir.getAbsLocalPath());
                        iir.image = ImageService.convertToTransparentPNG(iir.image, 8, origFile);
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
     * @param uri Could be "relativePath/to/image.jpg" or "https://a.b/image.jpg"
     * @param type Flags to determine if the image is cached locally, the resource directory,
     *        and whether it is a single image or a sprite sheet.
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

    /**
     * Deletes the file on the local machine that matches the image id.
     */
    @Override
    public boolean deleteImage(String id) throws IOException {
        // ***** STUDENT MUST IMPLEMENT THIS *****
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isAnimated(String id) {
        return sheets.containsKey(id);
    }

    /**
     * Reads the png/jpg file for the given image and saves the BufferedImage into
     * the ImageInfoRecord.
     * 
     * @param iir The ImageInfoRecord to use to find the file and save the BufferedImage
     * @return Image (the BufferedImage)
     * @throws IOException
     */
    private Image loadImageFromFile(ImageInfoRecord iir) throws IOException {
        File file = new File(iir.getAbsLocalPath());
        iir.image = ImageIO.read(file);
        return iir.image;
    }

    /**
     * Loads the image from the URL
     * @param iir The ImageInfoRecord that contains information about the image
     * @return Image found at the URL
     * @throws IOException
     */
    private Image loadImageFromUrl(ImageInfoRecord iir) throws IOException {
        // ***** STUDENT MUST IMPLEMENT THIS *****
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
}