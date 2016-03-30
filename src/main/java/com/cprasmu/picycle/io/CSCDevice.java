package com.cprasmu.picycle.io;

public interface CSCDevice {
	
	public long getTotalWheelRevolutions();
	public long getTotalCrankRevolutions();
	public long getLastCrankTime();
	public long getLastWheelTime();
	
}
