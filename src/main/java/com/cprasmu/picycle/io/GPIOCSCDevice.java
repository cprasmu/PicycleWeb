package com.cprasmu.picycle.io;

import com.cprasmu.picycle.CSCMetrics;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.*;
import com.pi4j.wiringpi.GpioUtil;

public class GPIOCSCDevice implements GpioPinListenerDigital, CSCDevice {
	
	private long lastWheelTime = System.currentTimeMillis();;
	private long lastCrankTime = System.currentTimeMillis();;
	private long totalWheelRevolutions = 0;
	private long totalCrankRevolutions = 0;
	private static final long INITIAL_MIN_PULSE_TIME = 50;
	private boolean ioinit =  false;
	private final GpioController gpio = GpioFactory.getInstance();
	private Pin crankIOPin;
	private Pin wheelIOPin;
	private long minPulseTime = INITIAL_MIN_PULSE_TIME;
	private CSCMetrics parent;
	
	public GPIOCSCDevice (CSCMetrics parent, int wheelPin, int crankPin) {
		
		this.parent = parent;
		GpioUtil.enableNonPrivilegedAccess();
		wheelIOPin = RaspiPin.getPinByAddress(wheelPin);
		crankIOPin = RaspiPin.getPinByAddress(crankPin);
		
		init();
	}
	
	public void resetGPIO() {
		
		gpio.shutdown();
		GpioPin gppin = null;
		
		for (GpioPin apin: gpio.getProvisionedPins()){
			gppin = apin;
			if (gppin!=null){
				gpio.unprovisionPin(gppin);
			}
		}
		ioinit =  false;
		init();
	}
	
	private void init() {
		if (ioinit) {
			return;
		}
		
		final GpioPinDigitalInput wheelSensor = gpio.provisionDigitalInputPin(wheelIOPin,  PinPullResistance.PULL_DOWN);
		wheelSensor.setDebounce(100, PinState.HIGH);
		wheelSensor.addListener(this);
		
		final GpioPinDigitalInput crankSensor = gpio.provisionDigitalInputPin(crankIOPin,  PinPullResistance.PULL_DOWN);
		crankSensor.setDebounce(100, PinState.HIGH);
		crankSensor.addListener(this);
		
		System.out.println("All I/O Initialized");
		ioinit = true;
	}
	
	@Override
	public long getLastWheelTime() {
		return lastWheelTime;
	}
	
	private void setLastWheelTime(long lastWheelTime) {
		this.lastWheelTime = lastWheelTime;
	}
	
	@Override
	public long getLastCrankTime() {
		return lastCrankTime;
	}
	
	private void setLastCrankTime(long lastCrackTime) {
		this.lastCrankTime = lastCrackTime;
	}
	
	@Override
	public long getTotalWheelRevolutions() {
		return totalWheelRevolutions;
	}
	
	private void setTotalWheelRevolutions(long totalWheelRevolutions) {
		this.totalWheelRevolutions = totalWheelRevolutions;
	}
	
	@Override
	public long getTotalCrankRevolutions() {
		return totalCrankRevolutions;
	}
	
	private void setTotalCrankRevolutions(long totalCrankRevolutions) {
		this.totalCrankRevolutions = totalCrankRevolutions;
	}

	@Override
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		
		 boolean update = false;
		 
		 if (event.getState().isHigh()) { 
			 long thisTime = System.currentTimeMillis();
			 if  (event.getPin().getName().equals(wheelIOPin.getName())) {
				 if ((System.currentTimeMillis() - lastWheelTime) > minPulseTime) {
	     				setTotalWheelRevolutions(getTotalWheelRevolutions()+1);
	     				setLastWheelTime(thisTime);
	     				update = true;
	     				
	     		 }
			 } else if  (event.getPin().getName().equals(crankIOPin.getName())) {
				 if ((System.currentTimeMillis() - lastCrankTime) > minPulseTime) {
	     				setTotalCrankRevolutions(getTotalCrankRevolutions()+1);
	     				setLastCrankTime(thisTime);
	     				update = true;
	     		 }
			 }  
		 }
		 if ((update) && (parent!=null)){
				parent.onNewValues(getTotalWheelRevolutions(), getLastWheelTime() , getTotalCrankRevolutions(), getLastCrankTime());
		 }
	}	
}
