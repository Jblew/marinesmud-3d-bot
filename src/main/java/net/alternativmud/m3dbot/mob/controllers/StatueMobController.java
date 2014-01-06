/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.alternativmud.m3dbot.mob.controllers;

import net.alternativmud.m3dbot.mob.MobController;

/**
 *
 * @author teofil
 */
public class StatueMobController extends MobController {
    private long lastTickTime = -1;

    public StatueMobController(String sceneName, float posX, float posY, float posZ) {
        super(sceneName);
        setPosition(posX, posY, posZ);
    }

    @Override
    public void tick(float posX, float posY, float posZ, float rotX, float rotY, float rotZ) {
        float deltaTime = 0;
        long currentTime = System.currentTimeMillis();
        if(lastTickTime != -1) {
            deltaTime = ((float)(currentTime-lastTickTime)/1000f);
        }
        lastTickTime = currentTime;
        
        rotY += deltaTime;
        setRotation(rotX, rotY, rotZ);
    }

    
}
