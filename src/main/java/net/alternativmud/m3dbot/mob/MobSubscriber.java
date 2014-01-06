/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.alternativmud.m3dbot.mob;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.alternativmud.logic.world.characters.UCharacter;
import net.alternativmud.system.nebus.server.AuthenticatedBusSubscriber.Characters;
import net.alternativmud.system.nebus.server.AuthenticatedBusSubscriber.EnterUnity3DMode;
import net.alternativmud.system.nebus.server.AuthenticatedBusSubscriber.GetCharacters;
import net.alternativmud.system.nebus.server.NetworkBusClient;
import net.alternativmud.system.nebus.server.StandardBusSubscriber.LoginFailed;
import net.alternativmud.system.nebus.server.StandardBusSubscriber.LoginSucceeded;

/**
 *
 * @author teofil
 */
class MobSubscriber {
    private final MobManager manager;
    private final Mob mob;
    private final NetworkBusClient eBus;

    public MobSubscriber(MobManager manager, Mob mob, NetworkBusClient eBus) {
        this.manager = manager;
        this.mob = mob;
        this.eBus = eBus;
    }

    @Subscribe
    public void loginSucceeded(LoginSucceeded evt) {
        Logger.getLogger(MobSubscriber.class.getName()).log(Level.INFO, "Mob " + mob + " logged in");
        eBus.post(new GetCharacters());
    }

    @Subscribe
    public void loginFailed(LoginFailed evt) {
        Logger.getLogger(MobSubscriber.class.getName()).log(Level.WARNING, "Mob " + mob + " login failed");
        try {
            eBus.close();
        } catch (IOException ex) {
            Logger.getLogger(MobSubscriber.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Subscribe
    public void gotCharacters(Characters characters) {
        UCharacter character = null;
        for (UCharacter chr : characters.getCharacters()) {
            if (chr.getName().equals(mob.getCharacter())) {
                character = chr;
                break;
            }
        }
        if (character == null) {
            Logger.getLogger(MobSubscriber.class.getName()).log(Level.WARNING, "Mob {0} does not own character {1}", new Object[]{mob, mob.getCharacter()});
            try {
                eBus.close();
            } catch (IOException ex) {
                Logger.getLogger(MobSubscriber.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            Logger.getLogger(MobSubscriber.class.getName()).log(Level.INFO, "Mob {0} will use character {1}", new Object[]{mob, character.getName()});
            eBus.register(mob.getController());
            manager.startController(mob.getController(), mob.getCharacter(), eBus);
        }
    }
}
