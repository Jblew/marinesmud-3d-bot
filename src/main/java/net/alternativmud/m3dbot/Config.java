/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.alternativmud.m3dbot;

import java.net.InetSocketAddress;
import net.alternativmud.m3dbot.mob.Mob;
import net.alternativmud.m3dbot.mob.controllers.StatueMobController;

/**
 *
 * @author teofil
 */
public class Config {
    public static final InetSocketAddress SERVER_ADDRESS = new InetSocketAddress("127.0.0.1", 8888);
    public static final int PACKETS_PER_SECOND = 5;
    public static final Mob [] MOBS = new Mob [] {
        new Mob("mob_statue", "mob_statue", "mob_statue", new StatueMobController("Passage", 1.5f, 0f, 5.5f)),
        new Mob("mob_ninja", "mob_ninja", "mob_ninja", new StatueMobController("Passage", -1f, 0f, 3f))
    };
}
