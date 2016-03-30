

package com.cprasmu.picycle.io.btle;

import java.io.IOException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Hex;


public class WriteServiceJob
extends CharacteristicsJob {
    private static Logger log = LoggerFactory.getLogger((Class)WriteServiceJob.class);
    BluezConnector.WriteController controller;

    public WriteServiceJob(String mac, String uuid, BluezConnector.WriteController controller) {
        super(mac, uuid);
        this.controller = controller;
    }

    @Override
    public void doJob() throws IOException {
        try {
            byte[] writeMessage;
            String writeHandle = this.getCharacteristicsHandle(this.uuid);
            log.info("got write handle = " + writeHandle);
            if (writeHandle == null) {
                throw new IOException("got no read handle: " + writeHandle);
            }
            int writtenCount = 0;
            while ((writeMessage = this.controller.next(this.mac)) != null) {
                String writeData = new String(Hex.encodeHex((byte[])writeMessage));
                this.currentGattProcess = this.connector.execute(new String[]{"gatttool", "-b", this.mac, "-t", "random", "-l", "medium", "--char-write-req", "-a", "0x" + writeHandle, "-n", writeData});
                int exitValue = this.currentGattProcess.waitFor();
                if (exitValue == 0) {
                    this.controller.onSuccessPartMessage(this.mac, writeMessage, writeMessage.length);
                    writtenCount += writeMessage.length;
                    continue;
                }
                String error = Utils.readStreamToString(this.currentGattProcess.getErrorStream());
                throw new IOException("failed to write, exit code: " + exitValue + " " + error);
            }
            this.controller.onSuccess(this.mac, writtenCount);
        }
        catch (InterruptedException e) {
            throw new IOException("interrupted write", e);
        }
    }

    @Override
    protected void onFailed(String msg, Exception e) {
        this.controller.onFailed(this.mac, msg, e);
    }

    @Override
    public void onRetry(int count, Exception e) {
        this.controller.onRetry(this.mac, count, e);
    }
}