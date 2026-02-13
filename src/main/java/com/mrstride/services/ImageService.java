
package com.mrstride.services;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

public interface ImageService {
    public static final int SPRITESHEET_TYPE = 1; // else is a regular image
    public static final int LOCAL_STORAGE_TYPE = 2; // else not saved to disk
    public static final int ROOT_STORAGE_TYPE = 4; // stored in root directory (default)
    public static final int URL_SOURCE_TYPE = 8; // URI is a URL. else File
    public static final int CONVERT_TO_PNG = 16;

    public static final int NORMAL_TYPE = ImageService.LOCAL_STORAGE_TYPE | ImageService.ROOT_STORAGE_TYPE;

    /**
     * Returns the set of ids that are images.
     * 
     * @return set of ids
     */
    public Set<String> getImages();

    /**
     * Returns the set of ids that are sprite sheets.
     * 
     * @return set of ids
     */
    public Set<String> getSpriteSheets();

    /**
     * Gets an image with the identifier.
     *
     * @param identifier Unique identifier of the image as added
     * @return a BufferedImage object
     * @throws IOException if the image cannot be found/downloaded
     */
    public BufferedImage getImage(String identifier) throws IOException;

    /**
     * Adds image information to the service so that it can be properly managed.
     *
     * @param id   A Unique string to identify this image. It is the Key.
     * @param uri  A relative file path to the image, or the URL to download the
     *             image from
     * @param type A set of flags that define the image using the constants.
     * @throws FileNotFoundException
     */
    public void addImageInfo(String id, String uri, int type) throws FileNotFoundException;

    /**
     * deleteImage is provided to allow better JUnit Tests to cleanup
     * after itself. It deletes an image from local storage.
     */
    // public boolean deleteImage(String identifier) throws IOException;
    // public void addSheet(String id, SpriteSheetInfo ssi);
    // public SpriteSheetInfo getSpriteSheetInfo(String id);
    // public boolean isAnimated(String id);
    /**
     * Efficiently resize the image
     *
     * @param originalImage
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public static BufferedImage resize(BufferedImage originalImage, int targetWidth, int targetHeight) {
        double scaleX = (double) targetWidth / originalImage.getWidth();
        double scaleY = (double) targetHeight / originalImage.getHeight();
        AffineTransform transform = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        op.filter(originalImage, resizedImage);
        return resizedImage;
    }

    public static BufferedImage flipHorizontally(BufferedImage originalImage) {
        AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
        transform.translate(-originalImage.getWidth(), 0);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(originalImage, null);
    }

    /**
     * deleteImage is provided to allow better JUnit Tests to cleanup
     * after itself. It deletes an image from local storage.
     */
    public boolean deleteImage(String identifier) throws IOException;

    public void addSheet(String id, SpriteSheetInfo ssi);
    public SpriteSheetInfo getSpriteSheetInfo(String id);
    
    public boolean isAnimated(String id);

    public static BufferedImage convertToTransparentPNG(BufferedImage image, int i, File origFile) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'convertToTransparentPNG'");
    }

    /*
     * The Student may want to add more public static helper methods to do things such
     * as:
     * 
     * 1) resize
     * 2) flipHorizontally
     * 3) rotateImage
     */

}
