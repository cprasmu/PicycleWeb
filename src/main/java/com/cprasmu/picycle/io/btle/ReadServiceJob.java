package com.cprasmu.picycle.io.btle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ReadServiceJob extends CharacteristicsJob {
	public BluezConnector.CharacteristicsListener listener;
    public ReadServiceJob(String mac, String uuid, BluezConnector.CharacteristicsListener listener) {
        super(mac, uuid);
        this.listener = listener;
    }

    public void doJob() {
        try {
            String readHandle = getCharacteristicsHandle(uuid);
            CharacteristicsThread.log.info((new StringBuilder("got write handle = ")).append(readHandle).toString());
            if (readHandle == null) {
                throw new IOException((new StringBuilder("got no read handle: ")).append(readHandle).toString());
            }
            currentGattProcess = connector.execute(new String[] {
                "gatttool", "-b", mac, "-t", "random", "-l", "medium", "--char-read", "-a", (new StringBuilder("0x")).append(readHandle).toString()
            });
            BufferedReader readerStdio = new BufferedReader(new InputStreamReader(currentGattProcess.getInputStream()));
            String line;
            for(boolean readMore = true; readMore && (line = readerStdio.readLine()) != null;)
                if(line.length() != 0)
                {
                    String EXP_READ_VALUE = "descriptor: ";
                    int idx = line.indexOf(EXP_READ_VALUE);
                    if(idx >= 0) {
                        String valuesData = line.substring(idx + EXP_READ_VALUE.length(), line.length());
                        valuesData = valuesData.replaceAll("\\s+", "");
                        byte rawMessage[] = UtilsByteBuffer.convertHexStringToByteArray(valuesData);
                        readMore = listener.onSuccess(mac, rawMessage);
                    }
                }

        }
        catch(IOException e) {
            CharacteristicsThread.log.error("failed to obtain handle: ", e);
            if(listener != null)
                listener.onFailed(mac, e.getMessage(), e);
        }
    }

    protected void onFailed(String msg, Exception e)
    {
        if(listener != null)
            listener.onFailed(mac, msg, e);
    }

    public void onRetry(int count, Exception e)
    {
        if(listener != null)
            listener.onRetry(mac, count, e);
    }

}
