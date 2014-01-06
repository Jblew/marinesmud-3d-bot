/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.alternativmud.m3dbot.mob;

/**
 *
 * @author teofil
 */
public class Mob {
    private final String login;
    private final String password;
    private final String character;
    private final MobController controller;

    public Mob(String login, String password, String character, MobController controller) {
        this.login = login;
        this.password = password;
        this.character = character;
        this.controller = controller;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getCharacter() {
        return character;
    }

    public MobController getController() {
        return controller;
    }

    @Override
    public String toString() {
        return login + ":" + character;
    }
    
    
}
