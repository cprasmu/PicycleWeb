package com.cprasmu.picycle;

public class Csc implements CSCMetrics {
    
    private long prevWheelEventTime = -1;
    private long prevCrankEventTime = -1;
    private long prevCumulativeWheelRevolutions = -1;
    private long prevCumulativeCrankRevolutions = -1;
    private double wheelRpm = 0;
    private double crankRpm = 0;

    public double getWheelRpm() {
        return wheelRpm;
    }
    
    public double getCrankRpm() {
        return crankRpm;
    }

    public long getumulativeWheelRevolutions(){
    	return prevCumulativeWheelRevolutions;
    }
    
    public long getumulativeCrankRevolutions(){
    	return prevCumulativeCrankRevolutions;
    }
    
    public boolean onNewValues(long cumulativeWheelRevolutions, long lastWheelEventTime, long cumulativeCrankRevolutions, long lastCrankEventTime) {
     //   Log.debug(TAG, "onNewValues(" + cumulativeWheelRevolutions + ", " + lastWheelEventTime + ", " + cumulativeCrankRevolutions + ", " + lastCrankEventTime + ")");

        boolean result = false;

        if (prevWheelEventTime > -1 && lastWheelEventTime > 0) {
            if (lastWheelEventTime != prevWheelEventTime) {
                wheelRpm = 60 * 1024f / ((lastWheelEventTime - prevWheelEventTime + 65536) % 65536);
                if (prevCumulativeWheelRevolutions > -1 && cumulativeWheelRevolutions > prevCumulativeWheelRevolutions + 1) {
                    wheelRpm = wheelRpm * (cumulativeWheelRevolutions - prevCumulativeWheelRevolutions);
            //        Log.v(TAG, "wheelRpm2: " + wheelRpm);
                }
           //     Log.info(TAG, "wheelRpm: " + wheelRpm);
          //      Log.v(TAG, "ratio: " + (crankRpm > 0 ? wheelRpm / crankRpm : 0));
                result = true;
            }
        }
        if (prevCrankEventTime > -1 && lastCrankEventTime > 0) {
            if (lastCrankEventTime != prevCrankEventTime) {
                crankRpm = 60 * 1024f / ((lastCrankEventTime - prevCrankEventTime + 65536) % 65536);
                if (prevCumulativeCrankRevolutions > -1 && cumulativeCrankRevolutions > prevCumulativeCrankRevolutions + 1) {
                    crankRpm = crankRpm * (cumulativeCrankRevolutions - prevCumulativeCrankRevolutions);
        //            Log.v(TAG, "crankRpm2: " + crankRpm);
                }
        //        Log.info(TAG, "crankRpm: " + crankRpm);
                result = true;
            }
        }
        if (lastWheelEventTime > 0) {
            prevCumulativeWheelRevolutions = cumulativeWheelRevolutions;
            prevWheelEventTime = lastWheelEventTime;
        }
        if (lastCrankEventTime > 0) {
            prevCumulativeCrankRevolutions = cumulativeCrankRevolutions;
            prevCrankEventTime = lastCrankEventTime;
        }
        return result;
    }
    
}