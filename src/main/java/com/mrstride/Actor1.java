package com.mrstride;

import org.springframework.stereotype.Component;

@Component
public class Actor1 implements MyBehavior {
    public void act() {
        System.out.println("I can act!");
    }
}