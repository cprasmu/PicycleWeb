package com.cprasmu.picycle.io.btle;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cprasmu.picycle.io.btle.BluezConnector.CharacteristicsListener;
import com.cprasmu.picycle.io.btle.BluezConnector.CharacteristicsWriteListener;

public class BluezAdvertisingThread extends Thread implements CharacteristicsListener, CharacteristicsWriteListener{
    
	private static Logger log = LoggerFactory.getLogger(com.cprasmu.picycle.io.btle.BluezAdvertisingThread.class);
    public List advertisingListeners;
    BluezScanThread scanner;
    BluezDumpThread dumper;
    private BluezConnector connector;
    
    BluezAdvertisingThread(BluezConnector connector) {
        super("BluezAdvertisingThread");
        advertisingListeners = new Vector();
        this.connector = connector;
    }

    public BluezConnector getConnector() {
        return connector;
    }

    public synchronized void addAdvertisingListener(BluezConnector.AdvertisingListener listener) {
        advertisingListeners.add(listener);
    }

    public synchronized void removeAdvertisingListener(BluezConnector.AdvertisingListener listener) {
        advertisingListeners.remove(listener);
    }

    public int getAdvertisingListenerCount() {
        return advertisingListeners.size();
    }

    public void startListening() {
        scanner = new BluezScanThread(this);
        scanner.start();
  //      dumper = new BluezDumpThread(this);
  //      dumper.start();
    }

    public void stopListening()  {
        if(scanner != null)
            scanner.terminate();
        if(dumper != null)
            dumper.terminate();
    }

    protected synchronized void notifyAdvertisingListeners(byte message[]) {
        for (int i = 0; i < advertisingListeners.size(); i++)
            try {
                ((BluezConnector.AdvertisingListener)advertisingListeners.get(i)).gotMessage(message);
            }
            catch(Exception e) {
                log.error("listener failed: ", e);
            }

    }
    protected synchronized void deviceFound(String mac){
    	
    	 String luuid = "00001816-0000-1000-8000-00805f9b34fb";
         ////            00002a5b-0000-1000-8000-00805f9b34fb
    	 String uuid = "00002a5b-0000-1000-8000-00805f9b34fb";
    	 
        // ReadServiceJob rsj = new ReadServiceJob(mac,uuid,this);
         // rsj.doJob();
         
         
         WriteServiceWithNotifyJob service = new WriteServiceWithNotifyJob(mac,uuid,luuid,this);
         service.setConnector(this.connector);
         try {
			service.doJob();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         String handle= "";
        // service.writeAndListen(handle,new byte[ (byte)1, (byte)0]);
         
        
          
    }

    public void run() {
        startListening();
    }

	@Override
	public boolean onSuccess(String s, byte[] abyte0) throws IOException {
		
		System.out.println(s);
		return false;
	}

	@Override
	public boolean onFailed(String s, String s1, Exception exception) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onRetry(String s, int i, Exception exception) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] doWrite(String s) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
