/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.alternativmud.m3dbot.system.bootstrap;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import net.alternativmud.m3dbot.App;
import net.alternativmud.system.lifecycle.RunnableTask;

/**
 *
 * @author teofil
 */
public class StartMobManager implements RunnableTask {    
    @Override
    public String getDescription() {
        return "Starts mob manager";
    }

    @Override
    public boolean shouldBeExecuted() {
        return !App.getApp().getMobManager().isRunning();
    }

    @Override
    public void execute() throws Exception {
        App.getApp().getMobManager().start();
        App.getApp().getLifecycle().registerShutdownTask(new RunnableTask() {

            public String getDescription() {
                return "Stops mob manager";
            }

            public boolean shouldBeExecuted() {
                return App.getApp().getMobManager().isRunning();
            }

            public void execute() throws Exception {
               App.getApp().getMobManager().stop();
            }
        });
    }
    
}
