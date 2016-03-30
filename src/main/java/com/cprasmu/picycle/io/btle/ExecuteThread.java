package com.cprasmu.picycle.io.btle;

import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteThread
extends Thread {
    private static Logger log = LoggerFactory.getLogger((Class)ExecuteThread.class);
    Process p;
    boolean isAlive = true;
    private String[] command;
    private OutputStream outputStdOut;
    private OutputStream outputStdErr;
    public Integer exitValue = null;
    private boolean doTerminate = false;
    private BluezConnector connector;

    public ExecuteThread(String name, String[] command, OutputStream outputStdOut, OutputStream outputStdErr, BluezConnector connector) throws IOException, InterruptedException {
        super(name);
        this.command = command;
        this.outputStdOut = outputStdOut;
        this.outputStdErr = outputStdErr;
        this.connector = connector;
        this.p = this.connector.execute(command);
    }

    public void terminate() {
        this.doTerminate = true;
    }

    @Override
    public void run() {
        try {
            InputStream stdout = this.p.getInputStream();
            InputStream stderr = this.p.getErrorStream();
            byte[] buff = new byte[1024];
            while (this.isAlive) {
                if (this.doTerminate) {
                    Integer pid = Utils.getProcessID(this.p);
                    if (pid != null) {
                        log.info("send sigint to " + pid);
                        try {
                            Runtime.getRuntime().exec("sudo kill -SIGINT " + pid);
                            Utils.sleepN(1000);
                        }
                        catch (Exception e) {
                            log.warn("failed to kill process: " + pid);
                        }
                    }
                    this.p.destroy();
                    this.isAlive = false;
                    continue;
                }
                int sumReadChars = 0;
                try {
                    int read;
                    if (stdout.available() > 0) {
                        int max = Math.min(stdout.available(), buff.length);
                        read = stdout.read(buff, 0, max);
                        this.outputStdOut.write(buff, 0, read);
                        sumReadChars += read;
                    }
                    if (stderr.available() > 0) {
                        int max = Math.min(stderr.available(), buff.length);
                        read = stderr.read(buff, 0, max);
                        this.outputStdErr.write(buff, 0, read);
                        sumReadChars += read;
                    }
                    if (sumReadChars != 0) continue;
                    try {
                        this.exitValue = this.p.exitValue();
                        this.isAlive = false;
                    }
                    catch (IllegalThreadStateException e) {
                        this.isAlive = true;
                        Utils.sleepN(500);
                    }
                    continue;
                }
                catch (IOException e1) {
                    log.error("execute failed: ", (Throwable)e1);
                }
            }
        }
        finally {
            try {
                this.outputStdErr.close();
                this.outputStdOut.close();
            }
            catch (IOException e) {
                log.error("failed to close stream", (Throwable)e);
            }
        }
    }
}