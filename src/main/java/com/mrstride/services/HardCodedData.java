package com.mrstride.services;

import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.mrstride.entity.Hero;
import com.mrstride.entity.MovingEntity;
import com.mrstride.entity.Entity;
import com.mrstride.gui.Line;

@Service
@Primary
@Qualifier("HardCodedData")
public class HardCodedData implements DataService {

    @Autowired
    private ImageService imageService;

    private static EntityManager entityMgr;

    private boolean imagesAdded = false;

    @Override
    public EntityManager loadLevel(int level) throws FileNotFoundException {
        addImages();

        entityMgr= new EntityManager();

        Entity e = new Entity("cloud", 200, 300, null);
        entityMgr.addEntity(e);

        Map<String, Object> map = new HashMap<>();
        map.put("isHit", Boolean.TRUE);
        map.put("isHero", Boolean.TRUE);
        
        MovingEntity ent = new Hero(null, 400, 300, 30, 40, map); // new Hero(null /*"goldStar.png"*/, 400, 300);
        entityMgr.addEntity(ent);

        int[][] floors = { { 0, 600, 1000, 620}, {1003, 650, 1090, 650}, {1090, 650, 1390, 600 }, 
            /* absolute bottom */ { -400, 700, 1800, 700}};
        for (int[] xy : floors) {
            Line line = new Line(xy[0], xy[1], xy[2], xy[3]);
            entityMgr.addFloor(line);
        } 

        Rectangle wall = new Rectangle(1001, 621, 5, 30);
        entityMgr.addWall(wall);
        wall = new Rectangle(0, 550, 10, 80);
        entityMgr.addWall(wall);

        return entityMgr;
    }

    public void addImages() throws FileNotFoundException {
        if (imagesAdded) {
            return;
        }
        // don't load the images again
        imagesAdded = true;

        // for now, we use only the cloud image
        imageService.addImageInfo("cloud", "cloud.png", ImageService.NORMAL_TYPE);
    }

    @Override
    public EntityManager getEntityMananger() {
        return entityMgr;
    }
    
}
