package com.cprasmu.picycle.io.btle;

import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacteristicsThread
extends Thread {
    static Logger log = LoggerFactory.getLogger((Class)CharacteristicsThread.class);
    LinkedBlockingQueue<CharacteristicsJob> jobQueue = new LinkedBlockingQueue();
    private boolean doExit;
    private BluezConnector connector;

    CharacteristicsThread(BluezConnector connector) {
        super("CharacteristicsThread");
        this.connector = connector;
    }

    public synchronized void readService(String mac, String uuid, int timeout, BluezConnector.CharacteristicsListener listener) {
        ReadServiceJob job = new ReadServiceJob(mac, uuid, listener);
        job.timeout = timeout;
        this.jobQueue.add(job);
    }

    public synchronized void writeService(String mac, String uuid, int timeout, BluezConnector.WriteController controller) {
        WriteServiceJob job = new WriteServiceJob(mac, uuid, controller);
        job.timeout = timeout;
        this.jobQueue.add(job);
    }

    public synchronized void writeServiceAndListen(String mac, String uuid, String uuidListen, int timeout, BluezConnector.CharacteristicsWriteListener listener) {
        WriteServiceWithNotifyJob job = new WriteServiceWithNotifyJob(mac, uuid, uuidListen, listener);
        job.timeout = timeout;
        this.jobQueue.add(job);
    }

    public void terminate() {
        this.doExit = true;
        this.interrupt();
    }

    @Override
    public void run() {
        while (!this.doExit) {
            try {
                CharacteristicsJob job = this.jobQueue.take();
                job.setConnector(this.connector);
                try {
                    log.info("stop advertising, job execute");
                    this.connector.stopAdvertising();
                    this.connector.resetBTDevice(this.connector.getDefaultActiveDevice());
                }
                catch (Exception e) {
                    log.error("failed to stop advertising", (Throwable)e);
                }
                job.doJobWithRetry();
            }
            catch (InterruptedException e) {
                log.error("interrupted characterisitcs thread");
                if (!this.jobQueue.isEmpty()) continue;
                log.info("start advertising, no jobs");
                this.connector.startAdvertising();
                continue;
            }
            catch (Throwable t) {
                try {
                    log.error("failed to execute job", t);
                }
                catch (Throwable var3_6) {
                    throw var3_6;
                }
                finally {
                    if (this.jobQueue.isEmpty()) {
                        log.info("start advertising, no jobs");
                        this.connector.startAdvertising();
                    }
                }
            }
            if (!this.jobQueue.isEmpty()) continue;
            log.info("start advertising, no jobs");
            this.connector.startAdvertising();
        }
    }
}