package com.mrstride.services;

import java.io.FileNotFoundException;

public interface DataService {
    
    public EntityManager getEntityMananger();
    
    public EntityManager loadLevel(int level) throws FileNotFoundException;
}
