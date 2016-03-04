package com.cprasmu.picycle.io;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class CSCDevice implements GpioPinListenerDigital {
	
	private long lastWheelTime = System.currentTimeMillis();;
	private long lastCrankTime = System.currentTimeMillis();;
	private long totalWheelRevolutions = 0;
	private long totalCrankRevolutions = 0;
	private boolean ioinit =  false;
	private final GpioController gpio = GpioFactory.getInstance();
	private Pin crankIOPin;
	private Pin wheelIOPin;
	private long MIN_PULSE_TIME = 100;
	
	public CSCDevice (int wheelPin, int crankPin) {
		
		wheelIOPin = RaspiPin.getPinByAddress(wheelPin);
		crankIOPin = RaspiPin.getPinByAddress(crankPin);
		
		initializeIO();
		
	}
	
	private void resetGPIO() {
		
		gpio.shutdown();
		GpioPin gppin = null;
		
		for (GpioPin apin: gpio.getProvisionedPins()){
			gppin = apin;
			if (gppin!=null){
				gpio.unprovisionPin(gppin);
			}
		}
	}
	
	private void initializeIO() {
		
		if (ioinit){
			return;
		}
		
		final GpioPinDigitalInput wheelSensor = gpio.provisionDigitalInputPin(wheelIOPin,  PinPullResistance.PULL_DOWN);
		wheelSensor.setDebounce(100, PinState.HIGH);
		wheelSensor.addListener(this);
	     
	     
	     /*
	     wheelSensor.addTrigger(new GpioCallbackTrigger(new Callable<Void>() {
	         public Void call() throws Exception {
	             if (wheelSensor.isHigh()) {
	            	long thisWheelTime = System.currentTimeMillis();
	     			if ((System.currentTimeMillis() - lastWheelTime) > 100) {
	     				setTotalWheelRevolutions(getTotalWheelRevolutions()+1);
	     				setLastWheelTime(thisWheelTime);
	     			}
	             }
				return null;
	         }
	     }));
	   
	     
	     wheelSensor.addListener(new GpioPinListenerDigital() {
	            @Override
	            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
	                if (event.getState().isHigh()) { 
	                	long thisCrankTime = System.currentTimeMillis();
		     			if ((System.currentTimeMillis() - lastCrankTime) > 100) {
		     				setTotalCrankRevolutions(getTotalCrankRevolutions()+1);
		     				setLastCrankTime(thisCrankTime);
		     			}
	                }
	            } 
	     });
	     */
	     
	     final GpioPinDigitalInput crankSensor = gpio.provisionDigitalInputPin(crankIOPin,  PinPullResistance.PULL_DOWN);
	     crankSensor.setDebounce(100, PinState.HIGH);
	     crankSensor.addListener(this);
	     
	     /* 
	     crankSensor.addTrigger(new GpioCallbackTrigger(new Callable<Void>() {
	         public Void call() throws Exception {
	             if (crankSensor.isHigh()) {
	            	 System.out.println("CRANK HIGH");
	            	 crankPulse();
	             }
	            
				return null;
	         }
	     }));
	    
	     
	     crankSensor.addListener(new GpioPinListenerDigital() {
	            @Override
	            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
	                // display pin state on console
	                
	                if (event.getState().isHigh()) { 
	                	long thisCrankTime = System.currentTimeMillis();
		     			if ((System.currentTimeMillis() - lastCrankTime) > 100) {
		     				setTotalCrankRevolutions(getTotalCrankRevolutions()+1);
		     				setLastCrankTime(thisCrankTime);
		     			}
	                }
	            } 
	     });
	      */
	    
	     System.out.println("All I/O Initialized");
	     ioinit = true;
	}
	
	
	
	
	public long getLastWheelTime() {
		return lastWheelTime;
	}
	
	private void setLastWheelTime(long lastWheelTime) {
		this.lastWheelTime = lastWheelTime;
	}
	
	public long getLastCrankTime() {
		return lastCrankTime;
	}
	
	private void setLastCrankTime(long lastCrackTime) {
		this.lastCrankTime = lastCrackTime;
	}
	
	public long getTotalWheelRevolutions() {
		return totalWheelRevolutions;
	}
	
	private void setTotalWheelRevolutions(long totalWheelRevolutions) {
		this.totalWheelRevolutions = totalWheelRevolutions;
	}
	
	public long getTotalCrankRevolutions() {
		return totalCrankRevolutions;
	}
	
	private void setTotalCrankRevolutions(long totalCrankRevolutions) {
		this.totalCrankRevolutions = totalCrankRevolutions;
	}

	@Override
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		
		 if (event.getState().isHigh()) { 
			 long thisTime = System.currentTimeMillis();
			 if  (event.getPin().equals(wheelIOPin)) {
				 if ((System.currentTimeMillis() - lastWheelTime) > MIN_PULSE_TIME) {
	     				setTotalWheelRevolutions(getTotalWheelRevolutions()+1);
	     				setLastWheelTime(thisTime);
	     		 }
			 } else if  (event.getPin().equals(crankIOPin)) {
				 if ((System.currentTimeMillis() - lastCrankTime) > MIN_PULSE_TIME) {
	     				setTotalCrankRevolutions(getTotalCrankRevolutions()+1);
	     				setLastCrankTime(thisTime);
	     		 }
			 }  
		 }
	}	
}
