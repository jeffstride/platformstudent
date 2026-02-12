package com.mrstride.services;

import java.io.FileNotFoundException;

public interface DataService {
    
    public static final String RESOURCES_PATH = "src/main/resources/";

    public EntityManager getEntityManager();
    public EntityManager loadLevel(int level) throws FileNotFoundException;
}
