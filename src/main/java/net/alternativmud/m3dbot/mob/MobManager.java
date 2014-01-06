/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.alternativmud.m3dbot.mob;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.alternativmud.lib.debug.BusLogger;
import net.alternativmud.m3dbot.Config;
import net.alternativmud.system.nebus.server.NetworkBusClient;
import net.alternativmud.system.nebus.server.StandardBusSubscriber;

/**
 *
 * @author teofil
 */
public class MobManager {
    private final ExecutorService executors = Executors.newCachedThreadPool();
    private final Map<Mob, NetworkBusClient> mobs = new HashMap<Mob, NetworkBusClient>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    public MobManager(EventBus systemBus) {
        for(Mob mob : Config.MOBS) {
            mobs.put(mob, null);
        }
    }
    
    public void start() {
        running.set(true);
        for(Mob mob : mobs.keySet()) {
            if(mobs.get(mob) == null) {
                try {
                    NetworkBusClient clientBus = new NetworkBusClient(Config.SERVER_ADDRESS);
                    clientBus.register(new BusLogger());
                    Logger.getLogger(MobManager.class.getName()).log(Level.INFO, "Mob {0} connected", mob);
                    clientBus.register(new MobSubscriber(this, mob, clientBus));
                    clientBus.post(new StandardBusSubscriber.PerformLogin(mob.getLogin(), mob.getPassword()));
                } catch (IOException ex) {
                    Logger.getLogger(MobManager.class.getName()).log(Level.SEVERE, "Could not create BusClient for Mob("+mob+")", ex);
                }
            }
        }
    }
    
    void startController(final MobController controller, final String characterName, final EventBus eBus) {
        executors.execute(new Runnable() {
            public void run() {
                controller.run(characterName, eBus);
            }
        });
    }
    
    public void stop() {
        executors.shutdown();
        try {
            executors.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
        }
        executors.shutdownNow();
        
        for(Mob mob : mobs.keySet()) {
            NetworkBusClient clientBus = mobs.get(mob);
            if(clientBus != null) {
                try {
                    clientBus.close();
                } catch (IOException ex) {
                    Logger.getLogger(MobManager.class.getName()).log(Level.SEVERE, "Exception while trying to close mob's BusClient", ex);
                }
            }
        }
        running.set(false);
    }
    
    public boolean isRunning() {
        return running.get();
    }
}
