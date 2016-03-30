package com.cprasmu.picycle.io;

import java.util.HashSet;
import java.util.Set;

//F7:E6:B7:91:42:FB
//gatttool -b F7:E6:B7:91:42:FB -t random -l high -I
//CSC_SERVICE @"1816"
//CSC_MEASUREMENT @"2A5B"
//CSC : attr handle: 0x000c, end grp handle: 0x0016 uuid: 00001816-0000-1000-8000-00805f9b34fb
//char-desc 0x000c
//handle: 0x000e, uuid: 00002a5b-0000-1000-8000-00805f9b34fb
//handle: 0x000f, uuid: 00002902-0000-1000-8000-00805f9b34fb
//write 0100 to 0x000f;

//BATTERY_SERVICE : attr handle: 0x0017, end grp handle: 0x001a uuid: 0000180f-0000-1000-8000-00805f9b34fb
//char-desc 0x0017
//handle: 0x0019, uuid: 00002a19-0000-1000-8000-00805f9b34fb
//hcitool lescan
//98:D6:BB:2B:ED:34 (unknown)
///98:D6:BB:2B:ED:34 (unknown)
//F7:E6:B7:91:42:FB COOSPO BK8
//F7:E6:B7:91:42:FB (unknown)

//gatttool -b F7:E6:B7:91:42:FB -t random -l low --primary
//gatttool -b F7:E6:B7:91:42:FB -t random -l low --characteristics
public class BTLECSCDevice implements CSCDevice {

	private CSCMeasurement status = new CSCMeasurement();
	private int batteryChargePercent = 0;
	
	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;
	private static final int GATT_SUCCESS=0;
	private String deviceMac ="";
	private Set<String>devices = new HashSet<String>();
	
	@Override
	public long getTotalWheelRevolutions() {
		// TODO Auto-generated method stub
		return status.getCumulativeWheelRevolutions();
	}

	@Override
	public long getTotalCrankRevolutions() {
		// TODO Auto-generated method stub
		return status.getCumulativeCrankRevolutions();
	}

	@Override
	public long getLastCrankTime() {
		// TODO Auto-generated method stub
		return status.lastCrankEventTime;
	}

	@Override
	public long getLastWheelTime() {
		// TODO Auto-generated method stub
		return status.lastWheelEventTime;
	}
	
	
	private CSCMeasurement decodePacket(String data) {
		
		CSCMeasurement response = new CSCMeasurement();
		
		//03  	2c de 00 00 cd f7 	81 52  17 88
		// F	WC			WT		CC		CT

		data = data.replace(" ", "");
		
		int flags = Integer.parseInt(data.substring(0, 1), 16 );
		
		int wc1 = Integer.parseInt(data.substring(2, 3), 16 );
		int wc2 = Integer.parseInt(data.substring(4, 5), 16 );
		int wc3 = Integer.parseInt(data.substring(6, 7), 16 );
		int wc4 = Integer.parseInt(data.substring(8, 9), 16 );
		
		int wt1 = Integer.parseInt(data.substring(10, 11), 16 );
		int wt2 = Integer.parseInt(data.substring(12, 13), 16 );
		
		int cc1 = Integer.parseInt(data.substring(14, 15), 16 );
		int cc2 = Integer.parseInt(data.substring(16, 17), 16 );
		
		int ct1 = Integer.parseInt(data.substring(18, 19), 16 );
		int ct2 = Integer.parseInt(data.substring(20, 21), 16 );
		
		response.setWheelRevolutionDataPresent((flags & 1 ) == 1);
		response.setCrankRevolutionDataPresent((flags & 2 ) == 2);
		
		response.setCumulativeWheelRevolutions(wc1 + (wc2 * 256) + (wc3 * 256*256) + (wc4 * 256*256*256));
		response.setLastWheelEventTime(wt1+(wt2*256));
		
		response.setCumulativeCrankRevolutions(cc1+(cc2*256));
		response.setLastCrankEventTime(ct1+(ct2*256));

		return response;
	}

	private class CSCMeasurement {

		boolean wheelRevolutionDataPresent = false;
		
		boolean crankRevolutionDataPresent = false;
		//UINT32
		private long cumulativeWheelRevolutions = -1;
		//UINT16
		private long lastWheelEventTime = -1;
		//UINT16
		private long cumulativeCrankRevolutions = -1;
		//UINT16
		private long lastCrankEventTime = -1;
		
		public boolean isWheelRevolutionDataPresent() {
			return wheelRevolutionDataPresent;
		}
		public void setWheelRevolutionDataPresent(boolean wheelRevolutionDataPresent) {
			this.wheelRevolutionDataPresent = wheelRevolutionDataPresent;
		}
		public boolean isCrankRevolutionDataPresent() {
			return crankRevolutionDataPresent;
		}
		public void setCrankRevolutionDataPresent(boolean crankRevolutionDataPresent) {
			this.crankRevolutionDataPresent = crankRevolutionDataPresent;
		}
		public long getCumulativeWheelRevolutions() {
			return cumulativeWheelRevolutions;
		}
		public void setCumulativeWheelRevolutions(long cumulativeWheelRevolutions) {
			this.cumulativeWheelRevolutions = cumulativeWheelRevolutions;
		}
		public long getLastWheelEventTime() {
			return lastWheelEventTime;
		}
		public void setLastWheelEventTime(long lastWheelEventTime) {
			this.lastWheelEventTime = lastWheelEventTime;
		}
		public long getCumulativeCrankRevolutions() {
			return cumulativeCrankRevolutions;
		}
		public void setCumulativeCrankRevolutions(long cumulativeCrankRevolutions) {
			this.cumulativeCrankRevolutions = cumulativeCrankRevolutions;
		}
		public long getLastCrankEventTime() {
			return lastCrankEventTime;
		}
		public void setLastCrankEventTime(long lastCrankEventTime) {
			this.lastCrankEventTime = lastCrankEventTime;
		}
		
	}
}
