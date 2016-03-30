package com.cprasmu.picycle.io.btle;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import com.cprasmu.picycle.io.btle.BluezConnector.AdvertisingListener;
import com.cprasmu.picycle.io.btle.BluezConnector.CharacteristicsListener;
import com.cprasmu.picycle.io.btle.BluezConnector.CharacteristicsWriteListener;

public class Test implements CharacteristicsListener,AdvertisingListener, CharacteristicsWriteListener {
	private enum ConnectionState {
		searching,foundDevice,connected;
	}
	//BC:6A:29:AE:CF:18 SensorTag
	//98:D6:BB:2B:ED:34
	//F7:E6:B7:91:42:FB CSC
	
	private ConnectionState state = ConnectionState.searching;
	
	public void test (){
		//log.info("start register!");
        final BluezConnector connector = new BluezConnector();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){

            @Override
            public void run() {
                //log.info("shutdown");
            	connector.close();
            }
        }));
        boolean foundDevice = checkBluetoothDevice(connector, 5);
        if (!foundDevice) {
        ///     log.error("FATAL: failed to get bluetooth device!");
             return;
         }
        
        
        String uuid = "00001816-0000-1000-8000-00805f9b34fb";
   	    String luuid = "00002a5b-0000-1000-8000-00805f9b34fb";
   	 
       // ReadServiceJob rsj = new ReadServiceJob(mac,uuid,this);
        // rsj.doJob();
        
        
        WriteServiceWithNotifyJob service = new WriteServiceWithNotifyJob("F7:E6:B7:91:42:FB",uuid,luuid,this);
        service.setConnector(connector);
        try {
			service.doJob();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        /*
        BluezAdvertisingThread btat = new BluezAdvertisingThread(connector);
        
        btat.addAdvertisingListener(this);
        btat.startListening();
       // BluezScanThread btst = new BluezScanThread(btat);
        
    //    btst.start();
      //  while(true) {
        	
	        try {
				Thread.sleep(1000);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
       // }
        
      //  String mac = "F7:E6:B7:91:42:FB";
       
       */
	}
	
	
	public boolean checkBluetoothDevice(BluezConnector connector, int retryCount) {
        boolean foundDevice = false;
        int i = 0;
        while (i < retryCount && !foundDevice) {
            try {
                List<String> devices = connector.getBTDevices();
                if (devices.size() <= 0) {
                    throw new IOException("no bluetooth device!");
                }
                boolean first = true;
                for (String dev : devices) {
                 //   log.info("disable bluetooth device: " + dev);
                    connector.setBTDeviceActive(dev, false);
                    if (first) {
                 //       log.info("enable bluetooth device: " + dev);
                        connector.setBTDeviceActive(dev, true);
                    }
                    first = false;
                }
                foundDevice = true;
            }
            catch (IOException | InterruptedException e1) {
          //      log.error("failed to fetch bluetooth devices", (Throwable)e1);
                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException dev) {
                    // empty catch block
                }
            }
            ++i;
        }
        return foundDevice;
    }


	@Override
	public boolean onSuccess(String s, byte[] abyte0) throws IOException {
		// TODO Auto-generated method stub
		System.out.println(s + ":" + abyte0);
		return false;
	}


	@Override
	public boolean onFailed(String s, String s1, Exception exception) {
		// TODO Auto-generated method stub
		System.out.println(s + ":" + exception);
		return false;
	}


	@Override
	public boolean onRetry(String s, int i, Exception exception) {
		// TODO Auto-generated method stub
		System.out.println(s + ":" + exception);
		return false;
	}


	@Override
	public void gotMessage(byte[] abyte0) throws ParseException {
		// TODO Auto-generated method stub
		System.out.println( abyte0);
		String mac =new String(abyte0);
		String uuid = "00002a5b-0000-1000-8000-00805f9b34fb";
	 //   ReadServiceJob rsj = new ReadServiceJob(mac,uuid,this);
        //[4, 62, 27, 2, 1, 0, 0, 52, -19, 43, -69, -42, -104, 15, 2, 1, 26, 11, -1, 76, 0, 9, 6, 2, 34, -64, -88, 1, 70, -95]
	    
	//    rsj.doJob();
	}


	@Override
	public byte[] doWrite(String s) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
