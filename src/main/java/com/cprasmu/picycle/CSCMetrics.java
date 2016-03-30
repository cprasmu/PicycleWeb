package com.cprasmu.picycle;

public interface CSCMetrics {
	
	public boolean onNewValues(long cumulativeWheelRevolutions, long lastWheelEventTime, long cumulativeCrankRevolutions, long lastCrankEventTime);
	
}
