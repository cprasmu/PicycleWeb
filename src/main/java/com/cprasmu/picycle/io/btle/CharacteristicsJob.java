// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CharacteristicsJob.java

package com.cprasmu.picycle.io.btle;

import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CharacteristicsJob extends Thread {
    private static Logger log = LoggerFactory.getLogger((Class)CharacteristicsJob.class);
    Process currentGattProcess;
    public String mac;
    public String uuid;
    public int timeout = 60000;
    public int retryCount = 10;
    public int retrySleepTime = 1000;
    private int currentRetry = 0;
    private int retryTimeout = 0;
    private boolean isTimeout = true;
    public boolean doDisconnectAfterJob = true;
    protected BluezConnector connector;

    public CharacteristicsJob(String mac, String uuid) {
        super("CJob" + mac);
        this.mac = mac;
        this.uuid = uuid;
    }

    public abstract void doJob() throws IOException;

    public abstract void onRetry(int var1, Exception var2);

    public void terminate() {
        try {
            if (this.currentGattProcess != null) {
                this.currentGattProcess.destroy();
            }
        }
        catch (Throwable t) {
            CharacteristicsThread.log.error("failed to destroy process", t);
        }
        this.interrupt();
    }

    public synchronized void doJobWithRetry() {
        this.isTimeout = true;
        this.currentRetry = 0;
        this.retryTimeout = 0;
        if (this.timeout <= 0) {
            throw new RuntimeException("wrong timeout parameter");
        }
        this.start();
        try {
            try {
                while (this.retryTimeout >= 0) {
                    this.wait(this.timeout);
                    this.retryTimeout -= this.timeout;
                }
            }
            catch (InterruptedException var1_1) {
                if (this.isTimeout) {
                    this.terminate();
                    this.onFailed("timeout of " + this.timeout, new BluezConnector.BluezTimeoutException("timeout " + this.timeout));
                }
            }
        }
        finally {
            if (this.isTimeout) {
                this.terminate();
                this.onFailed("timeout of " + this.timeout, new BluezConnector.BluezTimeoutException("timeout " + this.timeout));
            }
        }
    }

    protected abstract void onFailed(String var1, Exception var2);

    @Override
    public void run() {
        try {
            boolean success = false;
            while (!success && this.currentRetry <= this.retryCount) {
                try {
                    this.doJob();
                    this.isTimeout = false;
                    success = true;
                    continue;
                }
                catch (Exception e) {
                    ++this.currentRetry;
                    log.info("failed, retry: " + this.currentRetry + "/" + this.retryCount + " reason: " + e);
                    if (this.currentRetry > this.retryCount) {
                        try {
                            this.onFailed("failed", e);
                        }
                        catch (Exception e2) {
                            log.error("error handler failed: ", (Throwable)e);
                        }
                        continue;
                    }
                    if (this.currentRetry == 1) {
                        try {
                            log.info("try to remove connection uning bt-device for mac: " + this.mac);
                            this.connector.removeToken(this.mac);
                            this.retrySleepTime += 1000;
                        }
                        catch (Exception e1) {
                            log.warn("failed to remove device: " + this.mac + ", " + e1.getMessage());
                        }
                    } else {
                        try {
                            log.info("try to disconnect uning bt-device for mac: " + this.mac);
                            this.connector.disconnect(this.mac);
                        }
                        catch (Exception e1) {
                            log.warn("failed to remove device: " + this.mac + ", " + e1.getMessage());
                        }
                    }
                    Utils.sleepN(this.retrySleepTime);
                    this.retryTimeout = this.timeout;
                    try {
                        this.onRetry(this.currentRetry, null);
                        continue;
                    }
                    catch (Exception e4) {
                        log.error("onRetry handler failed: ", (Throwable)e4);
                    }
                }
            }
            if (this.doDisconnectAfterJob) {
                try {
                    log.info("disconnect from token: " + this.mac);
                    this.connector.disconnect(this.mac);
                }
                catch (IOException | InterruptedException e) {
                    log.error("failed to disconnect from token: " + this.mac);
                }
            }
        }
        finally {
            log.info("job ended");
            this.retryTimeout = -1;
            this.interrupt();
        }
    }

    public String getPrimaryHandle(String queryUUID) throws IOException {
        String line;
        this.currentGattProcess = this.connector.execute(new String[]{"gatttool", "-b", this.mac, "-t", "random", "--primary"});
        BufferedReader readerStdio = new BufferedReader(new InputStreamReader(this.currentGattProcess.getInputStream()));
        while ((line = readerStdio.readLine()) != null) {
        	System.out.println(line);
            int idxHandl;
            String exp;
            int posUUID = line.indexOf("uuid:");
            if (posUUID < 0 || line.indexOf(queryUUID, posUUID) < 0 || (idxHandl = line.indexOf(exp = "end grp handle = 0x")) < 0) continue;
            int idx = idxHandl + exp.length();
            String handle = line.substring(idx, idx + 4);
            return handle;
        }
        BufferedReader readerStderr = new BufferedReader(new InputStreamReader(this.currentGattProcess.getErrorStream()));
        while ((line = readerStderr.readLine()) != null) {
            CharacteristicsThread.log.info("error: " + line);
        }
        return null;
    }

    public String getCharacteristicsHandle(String queryUUID) throws IOException {
        String line;
        this.currentGattProcess = this.connector.execute(new String[]{"gatttool", "-b", this.mac, "-t", "random", "--characteristics"});
        BufferedReader readerStdio = new BufferedReader(new InputStreamReader(this.currentGattProcess.getInputStream()));
        while ((line = readerStdio.readLine()) != null) {
        	System.out.println(line);
            int idxHandl;
            String exp;
            int posUUID = line.indexOf("uuid =");
            if (posUUID < 0 || line.indexOf(queryUUID, posUUID) < 0 || (idxHandl = line.indexOf(exp = "char value handle = 0x")) < 0) continue;
            int idx = idxHandl + exp.length();
            String handle = line.substring(idx, idx + 4);
            return handle;
        }
        return null;
    }

    public void setConnector(BluezConnector connector) {
        this.connector = connector;
    }
}