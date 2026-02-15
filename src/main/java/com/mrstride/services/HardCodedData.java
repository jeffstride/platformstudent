package com.mrstride.services;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.mrstride.entity.Hero;
import com.mrstride.entity.MovingEntity;
import com.mrstride.entity.Entity;
import com.mrstride.gui.Line;

/**
 * This is a Temporary, Hard Coded implementation of the DataService. 
 * 
 * A DataService is responsible for:
 *   1) Adding images into the ImageService
 *   2) Creating all the entities, floors and walls for each level of the world
 *   3) Creating and Returning the EntityManager
 *   
 * This hard coded version will:
 *   1) manually add two images to the Image Service
 *   2) manually create the Entities to add to the world (or level).
 *   3) manually set the services and initialize each entity.
 * 
 * Future implementations will:
 *   1) Add images to the ImageService by reading a JSON file
 *   2) Read the level information from a JSON file
 *   3) Use an EntityFactory to create the Entities
 */
@Service
@Qualifier("HardCodedData")
public class HardCodedData implements DataService {

    @Autowired
    private ImageService imageService;

    private EntityManager entityMgr;
    private boolean imagesAdded = false;

    @Override
    public EntityManager loadLevel(int level) throws FileNotFoundException {
        // We need to initialize the ImageService with some images
        addImages();

        entityMgr= new EntityManager();

        Entity e = new Entity("cloud", 200, 300, null);
        entityMgr.addEntity(e);

        Map<String, Object> map = new HashMap<>();
        
        // Create the Hero entity
        MovingEntity hero = new Hero("goldStar", 400, 300, 30, 40, map);
        hero.setServices(null, imageService, null);
        hero.init();
        entityMgr.addEntity(hero);

        // Create the Cloud entity
        Entity cloud = new Entity("cloud", 500, 150, 150, 150, map);
        cloud.setServices(null, imageService, null);
        cloud.init();
        entityMgr.addEntity(cloud);

        int[][] floors = {
            {   0, 600, 1005, 620},
            {1003, 650, 1090, 650}, 
            {1090, 650, 1390, 600},
            {1800, 700, 1800, 300},
            {-400, 700, 1800, 700},
            {-400, 300, -400, 700},
            {1550, 610, 1650, 610},
            {1550, 610, 1400, 200},
            {-320, 615, -150, 520},
            {-150, 520,    0, 300},

            {1001, 621, 1001, 649},
            {1006, 621, 1006, 649},
            {0, 550, 0, 630},
            {-65, 600,  -55,  600}
        };
        for (int[] xy : floors) {
            Line line = new Line(xy[0], xy[1], xy[2], xy[3]);
            entityMgr.addFloor(line);
        }

        return entityMgr;
    }

    private void addImages() throws FileNotFoundException {
        // Make sure that we don't add images twice
        if (imagesAdded) {
            return;
        }

        // Set our flag so that we don't load the images again
        imagesAdded = true;

        // for now, we use only the two images
        imageService.addImageInfo("cloud", "cloud.png", ImageService.NORMAL_TYPE);
        imageService.addImageInfo("goldStar", "goldStar.png", ImageService.NORMAL_TYPE);
    }

    @Override
    public EntityManager getEntityManager() {
        return entityMgr;
    }
    
}
