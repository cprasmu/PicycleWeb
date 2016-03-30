package com.cprasmu.picycle.io.btle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluezDumpThread
extends Thread {
    private static final Logger log = LoggerFactory.getLogger((Class)BluezDumpThread.class);
    boolean doStop = false;
    boolean isTerminated = false;
    BufferedReader readerStdio = null;
    ExecuteThread executeThread = null;
    Hashtable<String, Long> ids = new Hashtable();
    private BluezAdvertisingThread advertisingThread;

    public BluezDumpThread(BluezAdvertisingThread bluezAdvertisingThread) {
        super("BluezDumpThread");
        this.advertisingThread = bluezAdvertisingThread;
    }

    public void terminate() {
        this.doStop = true;
        if (this.executeThread != null) {
            this.executeThread.terminate();
        }
        if (this.readerStdio != null) {
            this.interrupt();
        }
    }

    @Override
    public void run() {
        block10 : {
            try {
                while (!this.doStop) {
                    PipedInputStream stdoutIn = new PipedInputStream();
                    PipedOutputStream stdoutOut = new PipedOutputStream(stdoutIn);
                    this.readerStdio = new BufferedReader(new InputStreamReader(stdoutIn));
                    PipedInputStream stderrIn = new PipedInputStream();
                    PipedOutputStream stderrOut = new PipedOutputStream(stderrIn);
                    BufferedReader readerStderr = new BufferedReader(new InputStreamReader(stderrIn));
                    log.info("start dumper");
                    this.executeThread = new ExecuteThread("ExecHcidump", new String[]{"sudo", "hcidump", "--raw"}, stdoutOut, stderrOut, this.advertisingThread.getConnector());
                    this.executeThread.start();
                    String line = "";
                    ArrayList<Byte> byteLine = new ArrayList<Byte>();
                    boolean overread = true;
                    while (!this.doStop && (line = this.readerStdio.readLine()) != null) {
                        if (line == null || line.length() <= 0) continue;
                        String lineWithoutspace = line.replaceAll("\\s+", "");
                        int startIdx = 0;
                        if (lineWithoutspace.charAt(0) == '<') {
                            overread = true;
                        } else if (lineWithoutspace.charAt(0) == '>') {
                            overread = false;
                            startIdx = 1;
                            if (byteLine.size() > 0) {
                                this.notifyListeners(byteLine);
                            }
                            byteLine.clear();
                        }
                        if (overread) continue;
                        UtilsByteBuffer.addHexStringToByteArray(lineWithoutspace, startIdx, lineWithoutspace.length() - 1, byteLine);
                    }
                    log.info("stop dumper" + readerStderr.readLine());
                }
            }
            catch (IOException e) {
                if (!this.doStop) {
                    log.error("io error " + this.getName(), (Throwable)e);
                }
            }
            catch (InterruptedException e) {
                if (this.doStop) break block10;
                log.error("interrupted " + this.getName(), (Throwable)e);
            }
        }
    }

    private void notifyListeners(ArrayList<Byte> byteLine) {
        byte[] rawByte = new byte[byteLine.size()];
        int i = 0;
        while (i < byteLine.size()) {
            rawByte[i] = byteLine.get(i).byteValue();
            ++i;
        }
        this.advertisingThread.notifyAdvertisingListeners(rawByte);
    }

    public static byte[] hexStringToByteArray(ArrayList<Byte> bytes, String s) {
        int len = s.length();
        int i = 0;
        while (i < len) {
            byte b = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
            bytes.add(Byte.valueOf(b));
            i += 2;
        }
        return null;
    }
}