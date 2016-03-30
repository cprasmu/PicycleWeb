package com.cprasmu.picycle.io.btle;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluezConnector {
	 public static int DEFAULT_TIMEOUT = 30000;
	    private static Logger log = LoggerFactory.getLogger(com.cprasmu.picycle.io.btle.BluezConnector.class);
	    private BluezAdvertisingThread advertisingThread;
	    private CharacteristicsThread characteristicsThread;
	    private String defaultDevice;
    public static interface AdvertisingListener {

        public abstract void gotMessage(byte abyte0[])
            throws ParseException;
    }

    public static class BluezTimeoutException extends Exception {

        private static final long serialVersionUID = 0x5045a519L;

        public BluezTimeoutException(String s) {
            super(s);
        }

        public BluezTimeoutException(String s, Exception e) {
            super(s, e);
        }
    }

    public static interface CharacteristicsListener {

        public abstract boolean onSuccess(String s, byte abyte0[])
            throws IOException;

        public abstract boolean onFailed(String s, String s1, Exception exception);

        public abstract boolean onRetry(String s, int i, Exception exception);
    }

    public static interface CharacteristicsWriteListener  extends CharacteristicsListener {

        public abstract byte[] doWrite(String s) throws IOException;
    }

    public static class SensorNotAvaliableException extends Exception {

        public SensorNotAvaliableException(int sensorType, boolean fetchAlarmLog) {
            super((new StringBuilder("sensor not available ")).append(sensorType).append(" isAlarm ").append(fetchAlarmLog).toString());
        }
    }

    public static interface WriteController  {

        public abstract void onSuccessPartMessage(String s, byte abyte0[], int i);

        public abstract void onSuccess(String s, int i);

        public abstract void onFailed(String s, String s1, Exception exception);

        public abstract byte[] next(String s)  throws IOException;

        public abstract void onRetry(String s, int i, Exception exception);
    }


    public BluezConnector() {
        advertisingThread = null;
        characteristicsThread = null;
        defaultDevice = "hci0";
    }

    public synchronized void registerAdvertisingListener(AdvertisingListener listener) {
        log.info((new StringBuilder("REGISTER listener: ")).append(listener).toString());
        if (advertisingThread == null) {
            advertisingThread = new BluezAdvertisingThread(this);
            advertisingThread.start();
        }
        advertisingThread.addAdvertisingListener(listener);
    }

    public synchronized void deregisterAdvertisingListener(AdvertisingListener listern) {
        log.info((new StringBuilder("DEREGISTER listener: ")).append(listern).toString());
        if(listern == null)
            return;
        if(advertisingThread != null) {
            advertisingThread.removeAdvertisingListener(listern);
            if (advertisingThread.getAdvertisingListenerCount() == 0)  {
                advertisingThread.stopListening();
                advertisingThread = null;
            }
        }
    }

    public void readService(String mac, String uuid, CharacteristicsListener listener) {
        readService(mac, uuid, DEFAULT_TIMEOUT, listener);
    }

    public void readService(String mac, String uuid, int timeout, CharacteristicsListener listener) {
        checkCharacteristicsThread();
        characteristicsThread.readService(mac, uuid, timeout, listener);
    }

    public void writeService(String mac, String uuid, WriteController controller) {
        writeService(mac, uuid, DEFAULT_TIMEOUT, controller);
    }

    public void writeService(String mac, String uuid, int timeout, WriteController controller) {
        checkCharacteristicsThread();
        characteristicsThread.writeService(mac, uuid, timeout, controller);
    }

    public void writeServiceAndListen(String mac, String uuid, String listenUUID, CharacteristicsWriteListener listener)
    {
        writeServiceAndListen(mac, uuid, listenUUID, DEFAULT_TIMEOUT, listener);
    }

    public synchronized void writeServiceAndListen(String mac, String uuid, String listenUUID, int timeout, CharacteristicsWriteListener listener) {
        checkCharacteristicsThread();
        characteristicsThread.writeServiceAndListen(mac, uuid, listenUUID, timeout, listener);
    }

    private synchronized void checkCharacteristicsThread() {
        if(characteristicsThread == null)
        {
            characteristicsThread = new CharacteristicsThread(this);
            characteristicsThread.start();
        }
    }

    public synchronized void close() {
        if(characteristicsThread != null)
            characteristicsThread.terminate();
        if(advertisingThread != null)
        {
            advertisingThread.stopListening();
            advertisingThread = null;
        }
    }

    public List getBTDevices() throws IOException, InterruptedException {
        Process hcitoolProcess;
        int exitValue;
        Exception exception;
        log.info("start hcitool dev");
        
        hcitoolProcess = execute(new String[] {
            "hciconfig"
        });
        
        exitValue = hcitoolProcess.waitFor();
        exception = null;
        Object obj = null;
        BufferedReader readerdevStdio = new BufferedReader(new InputStreamReader(hcitoolProcess.getInputStream()));
        List devList;
        
        if(exitValue != 0) {
            String error = Utils.readStreamToString(hcitoolProcess.getErrorStream());
            throw new IOException(error);
        }
        
        devList = new ArrayList();
        String line;
        while((line = readerdevStdio.readLine()) != null) {
            if (line.length() > 0 && !Character.isWhitespace(line.charAt(0))) {
                String parts[] = line.split(":");
                devList.add(parts[0]);
            }
  
	        if (readerdevStdio != null) {
	            readerdevStdio.close();
	            break;
	        }
        }
       
       return devList;
    }

    public void removeToken(String mac) throws IOException, InterruptedException {
        Process p = execute(new String[] {
            "sudo", "bt-device", "-r", mac
        });
        int exitValue = p.waitFor();
        if(exitValue != 0)
        {
            String error = Utils.readStreamToString(p.getErrorStream());
            throw new IOException(error);
        } else
        {
            return;
        }
    }

    public void disconnect(String mac) throws IOException, InterruptedException {
        Process p = execute(new String[] {
            "sudo", "bt-device", "-d", mac
        });
        int exitValue = p.waitFor();
        if(exitValue != 0)
        {
            String error = Utils.readStreamToString(p.getErrorStream());
            throw new IOException(error);
        } else
        {
            return;
        }
    }

    public void setBTDeviceActive(String dev, boolean active)
        throws IOException, InterruptedException
    {
        String activeCmd = "down";
        if(active)
        {
            activeCmd = "up";
            defaultDevice = dev;
        }
        Process p = execute(new String[] {
            "sudo", "hciconfig", dev, activeCmd
        });
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            String error = Utils.readStreamToString(p.getErrorStream());
            throw new IOException(error);
        } else {
            return;
        }
    }

    public void resetBTDevice(String dev) throws IOException, InterruptedException {
        String activeCmd = "reset";
        Process p = execute(new String[] {
            "sudo", "hciconfig", dev, activeCmd
        });
        
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            String error = Utils.readStreamToString(p.getErrorStream());
            throw new IOException(error);
        } else {
            return;
        }
    }

    public Process execute(String params[])
        throws IOException
    {
        if(log.isInfoEnabled())  {
            StringBuffer sb = new StringBuffer("EXEC: ");
            String as[];
            int j = (as = params).length;
            for (int i = 0; i < j; i++) {
                String s = as[i];
                sb.append(s).append(" ");
            }

            log.info(sb.toString());
        }
        Process p = Runtime.getRuntime().exec(params);
        return p;
    }

    public void startAdvertising()
    {
        if(advertisingThread != null)
            advertisingThread.startListening();
        else
            log.info("no advertising listener");
    }

    public void stopAdvertising()
    {
        if(advertisingThread != null)
        {
            log.info("stop advertising and sleep");
            advertisingThread.stopListening();
            Utils.sleepN(2000L);
        }
    }

    public void updateTokenFirmware(String mac, File newFirmware)  throws IOException, InterruptedException {
        Process p = execute(new String[] {
            "sudo", "python", "dfu.py", "-f", newFirmware.getAbsolutePath(), "-a", mac
        });
        
        BufferedReader readerStdio = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while((line = readerStdio.readLine()) != null) 
            log.info((new StringBuilder("dfu: ")).append(line).toString());
        int exitValue = p.waitFor();
        if(exitValue != 0) {
            String error = Utils.readStreamToString(p.getErrorStream());
            throw new IOException(error);
        } else {
            return;
        }
    }

    public String getDefaultActiveDevice() {
        return defaultDevice;
    }

   

}
