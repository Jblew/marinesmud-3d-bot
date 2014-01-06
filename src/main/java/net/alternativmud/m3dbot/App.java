package net.alternativmud.m3dbot;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.alternativmud.lib.IdManager;
import net.alternativmud.m3dbot.mob.MobManager;
import net.alternativmud.m3dbot.mob.StartConsoleReader;
import net.alternativmud.m3dbot.system.bootstrap.StartMobManager;
import net.alternativmud.system.bootstrap.PrepareLogger;
import net.alternativmud.system.lifecycle.Lifecycle;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.commons.daemon.DaemonInitException;

/**
 * Author: Jedrzej Lew
 */
public class App {
    private static App INSTANCE;
    private final EventBus systemBus = new EventBus("system");
    private final Lifecycle lifecycle;
    private final MobManager mobManager;
    private DaemonController daemonController = null;
    
    public App() {
        try {
            new PrepareLogger().execute();
        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        lifecycle = new Lifecycle(systemBus);
        mobManager = new MobManager(systemBus);
        
        lifecycle.registerBootstrapTask(new StartConsoleReader());
        lifecycle.registerBootstrapTask(new StartMobManager());
    }

    /**
     * Daemon init
     */
    public void init(DaemonContext dc) throws DaemonInitException, Exception {
        daemonController = dc.getController();
        INSTANCE = this;
    }

    /**
     * Daemon init
     */
    public void init(String[] args) throws DaemonInitException, Exception {
        daemonController = null;
        INSTANCE = this;
    }

    /**
     * Daemon start
     */
    public void start() {
        lifecycle.bootstrap();
    }


    public static App getApp() {
        if (INSTANCE == null) {
            Logger.getLogger(App.class.getName()).severe("App is NOT initialized. Panic shutdown (Code: -1)!");
            System.exit(-1);
        }
        return INSTANCE;
    }

    public static void main(String[] args) throws Exception {
        INSTANCE = new App();
        INSTANCE.start();
    }

    /**
     * This method allows to start this App inside other App.
     */
    public static App embeddedInit() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException, IOException {
        INSTANCE = new App();
        new Thread(new Runnable() {

            @Override
            public void run() {
                INSTANCE.start();
            }
        }, "alternativmud-m3dbod-embedded-" + IdManager.getSessionSafe()).start();

        return INSTANCE;
    }

    /**
     * Daemon stop
     */
    public void stop() {
        getLifecycle().shutdown();
    }

    /**
     * Daemon destroy
     */
    public void destroy() {
    }
    
    public EventBus getSystemEventBus() {
        return systemBus;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public MobManager getMobManager() {
        return mobManager;
    }
}
