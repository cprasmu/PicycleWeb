package com.cprasmu.picycle.io.btle;

import java.io.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BluezScanThread extends Thread {
	 	private static Logger log = LoggerFactory.getLogger(com.cprasmu.picycle.io.btle.BluezScanThread.class);
	    boolean doStop;
	    boolean isTerminated;
	    BufferedReader readerStdio;
	    ExecuteThread executeThread;
	    Hashtable ids;
	    private BluezAdvertisingThread advertisingThread;
	    
    public BluezScanThread(BluezAdvertisingThread bluezAdvertisingThread) {
        super("BluezScanThread");
        doStop = false;
        isTerminated = false;
        readerStdio = null;
        executeThread = null;
        ids = new Hashtable();
        advertisingThread = bluezAdvertisingThread;
    }
    
    public Hashtable getIds(){
    	return ids;
    }
    
    public void terminate()  {
        doStop = true;
        
        if(executeThread != null){
            executeThread.terminate();
        }
        
        if(readerStdio != null){
            interrupt();
        }
    }

    public void run() {
        try {
            while(!doStop)  {
            	
                PipedInputStream stdoutIn = new PipedInputStream();
                PipedOutputStream stdoutOut = new PipedOutputStream(stdoutIn);
                readerStdio = new BufferedReader(new InputStreamReader(stdoutIn));
                PipedInputStream stderrIn = new PipedInputStream();
                PipedOutputStream stderrOut = new PipedOutputStream(stderrIn);
                BufferedReader readerStderr = new BufferedReader(new InputStreamReader(stderrIn));
                log.info("start scanner");
                executeThread = new ExecuteThread("ExecHcitoolLescan", new String[] {"sudo", "hcitool", "lescan" }, stdoutOut, stderrOut, advertisingThread.getConnector());
                executeThread.start();
                String line = "";
                ArrayList toRemove = new ArrayList();
                long lastCheck = System.currentTimeMillis();
                
                while((line = readerStdio.readLine()) != null) 
                    if(line != null)
                    {
                        long now = System.currentTimeMillis();
                        String outputs[] = line.split(" ");
                        if(outputs.length > 1 && outputs[0].length() > 15)
                            if (!ids.containsKey(outputs[0])) {
                               // if (log.isDebugEnabled()) {
                                    log.info((new StringBuilder("added: ")).append(outputs[0]).append("=").append(outputs[1]).toString());
                              //  }
                                advertisingThread.deviceFound(outputs[0]);
                                ids.put(outputs[0], Long.valueOf(now));
                                System.out.println(outputs[0]);
                            } else {
                                Long addtime = (Long)ids.get(outputs[0]);
                                if (now - addtime.longValue() > 5000L) {
                                    ids.put(outputs[0], Long.valueOf(System.currentTimeMillis()));
                                }
                            }
                        if (now - lastCheck > 5000L) {
                            toRemove.clear();
                            for (Iterator iterator = ids.keySet().iterator(); iterator.hasNext();) {
                                String key = (String)iterator.next();
                                if (now - ((Long)ids.get(key)).longValue() > 16000L) {
                                    toRemove.add(key);
                                }
                            }

                            for(Iterator iterator1 = toRemove.iterator(); iterator1.hasNext();) {
                                String key = (String)iterator1.next();
                                ids.remove(key);
                              //  if (log.isDebugEnabled()) {
                                    log.info((new StringBuilder("removed: ")).append(key).toString());
                             //   }
                            }

                        }
                    }
                log.info((new StringBuilder("stop scanner: ")).append(readerStderr.readLine()).toString());
            }
        }
        catch(IOException e) {
            if(!doStop)
                log.error((new StringBuilder("io error ")).append(getName()).toString(), e);
        }
        catch(InterruptedException e) {
            if(!doStop)
                log.error((new StringBuilder("interrupted ")).append(getName()).toString(), e);
        }
    }

}
