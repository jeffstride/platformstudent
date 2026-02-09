package com.mrstride.services;

import java.awt.image.BufferedImage;
import java.nio.file.Paths;

/**
 * An ImageInfoRecord represents a single image and is helpful to the ImageService.
 */
public class ImageInfoRecord {

    protected static final String PROJECT_PATH = "src/main/resources";

    public String id;
    public String uri;
    public String localPath;
    public BufferedImage image;

    public int type;

    public ImageInfoRecord(String id, String uri, int type) {
        this.id = id;
        this.uri = uri;
        this.type = type;
    }

    public boolean isLocalFile() {
        return (type & ImageService.URL_SOURCE_TYPE) == 0;
    }

    public boolean saveLocally() {
        return (type & ImageService.LOCAL_STORAGE_TYPE) > 0;
    }

    public String getAbsLocalPath() {
        if (localPath == null) {
            if (isLocalFile()) {
                // the given uri for the file is added to the subdirectory
                localPath = Paths.get(System.getProperty("user.dir"), ImageInfoRecord.PROJECT_PATH, 
                    getSubdirectory(), uri).toString();
            } else {
                // use the final part of uri as name
                // use only slashes
                String name = uri.replace("\\", "/");
                name = name.substring(name.lastIndexOf('/') + 1);
                // a Trace statement would probably be good here
                localPath = Paths.get(System.getProperty("user.dir"), ImageInfoRecord.PROJECT_PATH, 
                    getSubdirectory(), name).toString();
            }
        }
        
        return localPath;
    }

    public String getSubdirectory() {
        if ((type & ImageService.ROOT_STORAGE_TYPE) > 0) {
            return "images";
        }
        // If we add more types, then add more return values
        return "images";
    }

    public boolean isImage() {
        return (type & ImageService.SPRITESHEET_TYPE) == 0;
    }
    
    public boolean needToConvertToTransparent() {
        return (type & ImageService.CONVERT_TO_PNG) != 0;
    }    
}

