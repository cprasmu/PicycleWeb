// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   WriteServiceWithNotifyJob.java

package com.cprasmu.picycle.io.btle;

import java.io.*;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteServiceWithNotifyJob extends CharacteristicsJob {
	
    private static Logger log = LoggerFactory.getLogger((Class)WriteServiceWithNotifyJob.class);
    public String listenerUUID;
    public BluezConnector.CharacteristicsWriteListener listener;

    public WriteServiceWithNotifyJob(String mac, String uuid, String listenerUUID, BluezConnector.CharacteristicsWriteListener listener) {
        super(mac, uuid);
        this.listenerUUID = listenerUUID;
        this.listener = listener;
    }

    public void enableListen(String listenHandle) throws IOException {
        Process p = this.connector.execute(new String[]{"gatttool", "-b", this.mac, "-t", "random", "-l", "medium", "--char-write", "-a", "0x" + listenHandle, "-n", "0100"});
        try {
            int exitValue = p.waitFor();
            if (exitValue != 0) {
                String errorLine = Utils.readStreamToString(p.getErrorStream());
                log.error("error: " + errorLine);
                throw new IOException("failed to --char-write, exit code: " + exitValue + " " + errorLine);
            }
            CharacteristicsThread.log.info("write and listen exit val: " + p.exitValue());
        }
        catch (InterruptedException e) {
            CharacteristicsThread.log.error("inter", (Throwable)e);
        }
    }

    public String writeAndListen(String writeHandle, byte[] writeMessage) throws IOException {
        String writeData = new String(Hex.encodeHex((byte[])writeMessage));
        CharacteristicsThread.log.info(String.valueOf(this.mac) + " char-write-req: " + writeData);
        this.currentGattProcess = this.connector.execute(new String[]{"gatttool", "-b", this.mac, "-t", "random", "-l", "medium", "--char-write-req", "-a", "0x" + writeHandle, "-n", writeData, "--listen"});
        try {
            String line;
            BufferedReader readerStdio = new BufferedReader(new InputStreamReader(this.currentGattProcess.getInputStream()));
            boolean readMore = true;
            while (readMore && (line = readerStdio.readLine()) != null) {
                String EXP_VALUE;
                int idx;
                if (line.length() == 0 || (idx = line.indexOf(EXP_VALUE = "value: ")) < 0) continue;
                String valuesData = line.substring(idx + EXP_VALUE.length(), line.length());
                valuesData = valuesData.replaceAll("\\s+", "");
                byte[] rawMessage = UtilsByteBuffer.convertHexStringToByteArray(valuesData);
                readMore = this.listener.onSuccess(this.mac, rawMessage);
            }
        }
        finally {
            this.currentGattProcess.destroy();
            Utils.sleepN(1000);
        }
        return null;
    }

    @Override
    public void doJob() throws IOException {
        String listenHandle = this.getPrimaryHandle(this.listenerUUID);
        CharacteristicsThread.log.info("got listen handle = " + listenHandle);
        if (listenHandle == null) {
            throw new IOException("got no listen handle " + listenHandle);
        }
        String writeHandle = this.getCharacteristicsHandle(this.uuid);
        CharacteristicsThread.log.info("got write handle = " + writeHandle);
        if (writeHandle == null) {
            throw new IOException("got no write handle: " + writeHandle);
        }
       // this.enableListen(listenHandle);
        this.writeAndListen("000f", this.listener.doWrite(this.mac));
       // this.writeAndListen(writeHandle, this.listener.doWrite(this.mac));
    }

    @Override
    protected void onFailed(String msg, Exception e) {
        if (this.listener != null) {
            this.listener.onFailed(this.mac, msg, e);
        }
    }

    @Override
    public void terminate() {
        super.terminate();
    }

    @Override
    public void onRetry(int count, Exception e) {
        if (this.listener != null) {
            this.listener.onRetry(this.mac, count, e);
        }
    }
}
