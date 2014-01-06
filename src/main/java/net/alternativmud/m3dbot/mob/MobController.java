/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.alternativmud.m3dbot.mob;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.alternativmud.lib.IdManager;
import net.alternativmud.lib.NamingThreadFactory;
import net.alternativmud.m3dbot.Config;
import net.alternativmud.system.nebus.server.AuthenticatedBusSubscriber;
import net.alternativmud.system.unityserver.Unity3DModeSubscriber;

/**
 *
 * @author teofil
 */
public abstract class MobController {
    private final int packetIntervalMs = 1000 / Config.PACKETS_PER_SECOND;
    private final AtomicReference<DatagramSocket> socketReference = new AtomicReference<DatagramSocket>(null);
    private final ScheduledExecutorService writeExecutor = Executors.newSingleThreadScheduledExecutor(new NamingThreadFactory("mob-" + IdManager.getSessionSafe() + "-controller-udp-writer"));
    private final AtomicInteger port = new AtomicInteger(0);
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final String sceneName;
    private final Lock dataLock = new ReentrantLock();
    private float posX;
    private float posY;
    private float posZ;
    private float rotX;
    private float rotY;
    private float rotZ;
    private String characterName = null;
    private byte characterID = 0;

    protected MobController(String sceneName) {
        this.sceneName = sceneName;
    }

    public final void run(String characterName, EventBus eBus) {
        this.characterName = characterName;
        eBus.post(new AuthenticatedBusSubscriber.EnterUnity3DMode(sceneName, characterName));

        byte[] receiveData = new byte[512];
        byte[] sendData = new byte[25];
        long i = 0;
        while (running.get()) {
            long sTime = System.currentTimeMillis();
            DatagramSocket socket = socketReference.get();
            if (socket != null) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    ByteBuffer bb = ByteBuffer.wrap(receivePacket.getData());
                    //TODO: Add enemy support
                    /*while (bb.hasRemaining()) {
                     byte rCharacterID = bb.get();
                     if (rCharacterID != 0) {
                     float enemyPosX = 
                     offset += 4;
                     float posY = ReadSingleBigEndian(data, offset);
                     offset += 4;
                     float posZ = ReadSingleBigEndian(data, offset);
                     offset += 4;
                     float rotX = ReadSingleBigEndian(data, offset);
                     offset += 4;
                     float rotY = ReadSingleBigEndian(data, offset);
                     offset += 4;
                     float rotZ = ReadSingleBigEndian(data, offset);
                     offset += 4;
                     } else {
                     offset += 25;
                     }
                     }*/
                } catch (SocketTimeoutException ex) {
                    //don't do anything
                } catch (IOException ex) {
                    Logger.getLogger(MobController.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {
                    ByteBuffer bb = ByteBuffer.allocate(25);
                    dataLock.lock();
                    try {
                        bb.put(characterID);
                        bb.putFloat(posX);
                        bb.putFloat(posY);
                        bb.putFloat(posZ);
                        bb.putFloat(rotX);
                        bb.putFloat(rotY);
                        bb.putFloat(rotZ);
                    } finally {
                        dataLock.unlock();
                    }
                    sendData = bb.array();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
                    socket.send(sendPacket);
                } catch (SocketTimeoutException ex) {
                    //don't do anything
                } catch (IOException ex) {
                    Logger.getLogger(MobController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            dataLock.lock();
            try {
                tick(posX, posY, posZ, rotX, rotY, rotZ);
            } finally {
                dataLock.unlock();
            }
            
            if(i%(4000/packetIntervalMs) == 0) {//every 4 seconds send a hartbeat
                eBus.post(new HashMap<String, String>());
            }

            long ellapsedTime = System.currentTimeMillis() - sTime;
            if (ellapsedTime < packetIntervalMs) {
                try {
                    TimeUnit.MILLISECONDS.sleep(packetIntervalMs - ellapsedTime);
                } catch (InterruptedException ex) {
                }
            }
            i++;
        }
    }

    public final void stop() {
        running.set(false);
        if (socketReference.get() != null) {
            socketReference.get().close();
        }

        writeExecutor.shutdown();
        try {
            writeExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
        }
        writeExecutor.shutdownNow();
    }
    
    @Subscribe
    public void unity3DModeEnterFailed(AuthenticatedBusSubscriber.Unity3DModeEnterFailed evt) {
        Logger.getLogger(MobController.class.getName()).log(Level.WARNING, "Mob controller could not enter Unity3DMode (message=\"{0}\")", evt.getMessage());
    }

    @Subscribe
    public void sceneEnterSucceeded(Unity3DModeSubscriber.SceneEnterSucceeded evt) {
        this.characterID = evt.getCharacterID();
        this.port.set(evt.getPort());
        if (socketReference.get() == null) {
            try {
                socketReference.set(new DatagramSocket());
            } catch (SocketException ex) {
                Logger.getLogger(MobController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        socketReference.get().disconnect();
        try {
            socketReference.get().connect(Config.SERVER_ADDRESS.getAddress(), port.get());
            socketReference.get().setSoTimeout(20);
        } catch (SocketException ex) {
            Logger.getLogger(MobController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public final String getSceneName() {
        return sceneName;
    }

    public final String getCharacterName() {
        dataLock.lock();
        try {
            return characterName;
        } finally {
            dataLock.unlock();
        }
    }

    public final byte getCharacterID() {
        dataLock.lock();
        try {
            return characterID;
        } finally {
            dataLock.unlock();
        }
    }

    public final void setPosition(float posX, float posY, float posZ) {
        dataLock.lock();
        try {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        } finally {
            dataLock.unlock();
        }
    }

    public final void setRotation(float rotX, float rotY, float rotZ) {
        dataLock.lock();
        try {
            this.rotX = rotX;
            this.rotY = rotY;
            this.rotZ = rotZ;
        } finally {
            dataLock.unlock();
        }
    }

    public final void setPositionX(float posX) {
        dataLock.lock();
        try {
            this.posX = posX;
        } finally {
            dataLock.unlock();
        }
    }

    public final void setPositionY(float posY) {
        dataLock.lock();
        try {
            this.posY = posY;
        } finally {
            dataLock.unlock();
        }
    }

    public final void setPositionZ(float posZ) {
        dataLock.lock();
        try {
            this.posZ = posZ;
        } finally {
            dataLock.unlock();
        }
    }

    public final void setRotationX(float rotX) {
        dataLock.lock();
        try {
            this.rotX = rotX;
        } finally {
            dataLock.unlock();
        }
    }

    public final void setRotationY(float rotY) {
        dataLock.lock();
        try {
            this.rotY = rotY;
        } finally {
            dataLock.unlock();
        }
    }

    public final void setRotationZ(float rotZ) {
        dataLock.lock();
        try {
            this.rotZ = rotZ;
        } finally {
            dataLock.unlock();
        }
    }

    public final float getPositionX() {
        dataLock.lock();
        try {
            return this.posX;
        } finally {
            dataLock.unlock();
        }
    }

    public final float getPositionY() {
        dataLock.lock();
        try {
            return this.posY;
        } finally {
            dataLock.unlock();
        }
    }

    public final float getPositionZ() {
        dataLock.lock();
        try {
            return this.posZ;
        } finally {
            dataLock.unlock();
        }
    }

    public final float getRotationX() {
        dataLock.lock();
        try {
            return this.rotX;
        } finally {
            dataLock.unlock();
        }
    }

    public final float getRotationY() {
        dataLock.lock();
        try {
            return this.rotY;
        } finally {
            dataLock.unlock();
        }
    }

    public final float getRotationZ() {
        dataLock.lock();
        try {
            return this.rotZ;
        } finally {
            dataLock.unlock();
        }
    }

    public abstract void tick(float posX, float posY, float posZ, float rotX, float rotY, float rotZ);
}
