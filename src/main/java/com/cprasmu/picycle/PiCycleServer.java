package com.cprasmu.picycle;

import java.io.IOException;

//import javax.ejb.Singleton;
//import javax.ejb.Startup;
import com.cprasmu.picycle.io.*;
import com.cprasmu.picycle.model.*;

//@Startup
//@Singleton
public class PiCycleServer {
	
	private GPIOCSCDevice cscDevice;
	private ServoControlledTurboTrainer turboTrainer;
	private Csc csc = new Csc();
	private double wheelSize = 2.17;
	
	private PiCycleServer() {
		System.out.println("PiCycle Server starting");
		cscDevice = new GPIOCSCDevice(csc, 3,4);
		
		try {
			turboTrainer = new ServoControlledTurboTrainer(0);	
		} catch (IOException e) {
			System.err.println("Failed to initialise PWM controller : " + e.getMessage());
		}
		
	}
	
	public BikeStatus getBikeStatus() {
		
		BikeStatus response = new BikeStatus();
		
		response.setSpeed(csc.getWheelRpm() * 60 * wheelSize );
		response.setCadence(csc.getCrankRpm());
		response.setDistance(csc.getumulativeWheelRevolutions() * wheelSize);
		return response;
		
	}
	
	private static class SingletonHolder { 
	    private static final PiCycleServer INSTANCE = new PiCycleServer();
	}

	public static PiCycleServer getInstance() {
	    return SingletonHolder.INSTANCE;
	}
}
