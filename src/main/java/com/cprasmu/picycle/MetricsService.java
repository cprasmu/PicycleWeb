package com.cprasmu.picycle;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.cprasmu.picycle.io.PWMDevice;
import com.cprasmu.picycle.model.BikeJourney;
import com.cprasmu.picycle.model.ElevationPoint;
import com.cprasmu.picycle.model.Location;
import com.cprasmu.picycle.model.JourneyPoint;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import javax.servlet.annotation.WebListener;

//@WebListener()
public class MetricsService  {

	private Float deltaDistance = new Float(0);
	private Float totalDistance = new Float(0);
	private Float wheelDiameter = 2.08f;
	private Float wheelCadence = new Float(0);
	private double cadence = new Double(0);
	private boolean tripStarted=false;
	private long lastWheelTime=0;
	private double altitude = 0;
	private long pulseCount=0;
	private ArrayList<Float> speedList = new ArrayList<Float>(3);
	private boolean keepRunning = true;
	private static final int pwmFreq = 50;
	private static final int servoChan = 0;
	private final GpioController gpio = GpioFactory.getInstance();
	private static final float MIN_BIKE_LOAD = 1.0f;
	private static final float DEFAULT_BIKE_LOAD = 3.0f;
	private static final float MAX_BIKE_LOAD = 5.0f;
	
	private static final int PWM_START = 150;
	private static final int PWM_MFACTOR = 60;
	//private float maxMagLoad = 5.0f;
	private boolean ioinit =  false;
	
			
	public static final int NOT_SET = Integer.MIN_VALUE;
	private long lastSpin, spinDiff,thisSpin=System.currentTimeMillis();
    //method timer variables
    private long timerStart, timerFinish, timerDiff, totalTimes, numTimes=0, maxTime;
    private final long MILLISECONDS_PM = 60000;
    
    
    private ArrayList<BikeJourney>journeys = new ArrayList<>();
    BikeJourney currentJourney = new BikeJourney();
    
    
    /*
     * Data for power calculations
     * http://polynomialregression.drque.net/online.php
0,0
18,100
20,180
25,225
30,350
35,500
36,550
37,600
38,640
40,740
41,800
43,900
45,1000
48,1200
50,1350
52,1500
55,1750
58,2000
60,2200
    */

	private PWMDevice.PWMChannel servo0;
	
	private void resetGPIO() {
		
		gpio.shutdown();
		GpioPin gppin=null;
		
		for (GpioPin apin: gpio.getProvisionedPins()){
			gppin = apin;
			if (gppin!=null){
				gpio.unprovisionPin(gppin);
			}
		}
	}
	
	
	public void setBikeLoad(float load){
		
		if ((load < MIN_BIKE_LOAD) || (load > MAX_BIKE_LOAD)){
			return;
		}
		
		System.out.println("Set PWM load : " + (PWM_START + (int)((load-1) * PWM_MFACTOR)) + " for Bike load : "+ load) ;
		setPwmLoad(PWM_START + (int)((load-1) * PWM_MFACTOR));
		
	}
	
	
	public ArrayList<BikeJourney>getJourneys(){
		return journeys;
	}
	
	public void setPwmLoad(int load) {

		try {
			
			servo0.setPWM(0,load);
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	public void resetTrip () {
		
		deltaDistance = new Float(0);
		totalDistance = new Float(0);
		wheelCadence = new Float(0);
		lastWheelTime = System.currentTimeMillis();
		
		tripStarted = false;
		
	}
	
	public void setAltitude(double altitude){
		this.altitude=altitude;
	}
	
	private synchronized void initializeI2C(){
	
		try {
			PWMDevice device = new PWMDevice();
			device.setPWMFreqency(pwmFreq);
			
			servo0 = device.getChannel(servoChan);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setDefaultLoad(){
		setBikeLoad(DEFAULT_BIKE_LOAD);
	}
	
	private void initializeIO() {
		
		if (ioinit){
			return;
		}
		// resetGPIO();
		
		 // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
	     final GpioPinDigitalInput wheelSensor = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03,  PinPullResistance.PULL_DOWN);
	    // wheelSensor.setDebounce(100);
	     
	     wheelSensor.addTrigger(new GpioCallbackTrigger(new Callable<Void>() {
	         public Void call() throws Exception {
	             if (wheelSensor.isHigh()) {
	            	 wheelPulse();
	             }
				return null;
	         }
	     }));
	   
	     final GpioPinDigitalInput crankSensor = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04,  PinPullResistance.PULL_DOWN);
	     crankSensor.setDebounce(100, PinState.HIGH);
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
	     */
	     crankSensor.addListener(new GpioPinListenerDigital() {
	            @Override
	            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
	                // display pin state on console
	                
	                if (event.getState().isHigh()) {
	                	 System.out.println("[" + new Date() + "] STATE CHANGE: " + event.getPin() + " = " + event.getState());
	                	 crankPulse();
	                }
	            }
	            
	        });
	     initializeI2C();
	     setDefaultLoad();
	     System.out.println("All I/O Initialized");
	     ioinit=true;
	}
	
	private double calculatePower(double x){
		
		double result;

		double xx = x*x;
		double xxx = xx*x;
		double xxxx = xxx*x;
		double xxxxx = xxxx*x;
		double xxxxxx = xxxxx*x;
	
		result = -0.10129671505451972 - (30.233019453390963*x) + (5.1586949359111065*xx) - (0.286245217539615*xxx) + (0.007975447625589188*xxxx) -( 0.00010206945292925567*xxxxx) + (4.9894906712562e-7*xxxxxx);
		return result;		
		
	}
	
	private MetricsService() {
		
		System.out.println("###################### MetricsService created ######################");
		
		deltaDistance = new Float(0);
		totalDistance = new Float(0);
		wheelCadence = new Float(0);
		lastWheelTime = System.currentTimeMillis();

		Thread th = new Thread() {

            public synchronized void run() {
            	Thread.currentThread().setName("INITIALIZE_IO");
            	initializeIO();
            }

        };

        th.start();
        Thread y = new Thread();
        y.start();
        
		System.out.println("PiCycle Initialized ! ");
		
		
		Thread logger = new Thread() {

            public synchronized void run() {
            	
            	try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
                while (keepRunning){
                    try {
                        Thread.sleep(1000);

                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

        };

        logger.start();
		
	}
	
	public void setKeepRunning(boolean keepRunning){
		this.keepRunning = keepRunning;
	}
	
	private static class SingletonHolder { 
	    private static final MetricsService INSTANCE = new MetricsService();
	}

	public static MetricsService getInstance() {
	    return SingletonHolder.INSTANCE;
	}
	
	public synchronized long wheelPulse() {
		
		synchronized (deltaDistance) {
			deltaDistance += wheelDiameter;
			totalDistance += wheelDiameter;
			pulseCount++;
			
			long timeDiff = System.currentTimeMillis() - lastWheelTime;
			
			System.out.println("Speed : " + (wheelDiameter/(timeDiff/1000f))*2.25);
			
			wheelCadence = (float)(System.currentTimeMillis() - lastWheelTime)/1000f;
			lastWheelTime = System.currentTimeMillis();
			speedList.add((wheelDiameter/wheelCadence * 2.25f));
			
			if (speedList.size()>5) {
				speedList.remove(0);
			}
			return pulseCount;
		}
		
	}
	
	
	public float getSpeedMPH() {
		
		Float total=0f;
		if (speedList.size()==0) return 0;
		
		for (int i=0;i<speedList.size();i++){
			total += speedList.get(i);
		}
		
		return (total/speedList.size());
		
	}
	
	public synchronized void crankPulse(){
		
        //start timer
		if ((System.currentTimeMillis()-timerFinish)<100){
			System.out.println("IGNORING");
			return;
		}
        timerStart = System.currentTimeMillis();
       
        double rpm = 0;
        //lastSpin should be what thisSpin was assigned last time
        lastSpin = thisSpin;
        //thisSpin should be the current time
        thisSpin = System.currentTimeMillis();
        //the difference is the elapsed time
        spinDiff = thisSpin - lastSpin;
        
        if (spinDiff > 0) {
        	cadence = MILLISECONDS_PM/spinDiff;
        }
        
        //end timer
        timerFinish = System.currentTimeMillis();
        timerDiff = timerFinish - timerStart;
        numTimes++;
        //add time to total
        totalTimes = totalTimes + timerDiff;
        //if its the new max print it
        if (timerDiff > maxTime){
            maxTime = timerDiff;
        }
       
 
    }
	
	
	public Float consumeDelta(double lat,double lng) {
		
		synchronized (deltaDistance) {
			
			JourneyPoint jp = new JourneyPoint();
			jp.setTime(System.currentTimeMillis());
			jp.setDistance((double)deltaDistance);
			jp.setSpeed(getSpeedMPH());
			jp.setCadence(getCadence());
			jp.setPower(calculatePower(getSpeedMPH()*1.6));
			jp.setAltitude(altitude);
			jp.setGeoPoint(new Location(lat,lng));

			System.out.println(jp);
			writeFile(jp.toString());
			
			currentJourney.getData().add(jp);
			Float retval  = deltaDistance;
			deltaDistance = 0f;
			
			if (!retval.isInfinite()){
				return retval;
			} else {
				return 0f;
			}
			
		}
	}
	
	public BikeJourney getCurrentBikeJourney(){
		return currentJourney;
	}
	
	public static void writeFile(String line)  {
		
		FileWriter fw;
		
		try  {
			fw = new FileWriter("/tmp/bikelog.txt",true);
			
			fw.write(line + "\r\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public double getTotalDistance() {
		
		return totalDistance;
	}
	
	public double getCadence() {
		return cadence;
	}
	
	public long getTripTime() {
		return System.currentTimeMillis() - currentJourney.getStartDate();
		
	}
	
	public void reset() {
		
		pulseCount=0;
		deltaDistance=0f;
		totalDistance=0f;
		tripStarted = false;
	}
	
	public void start(String journeyName) {
		
		currentJourney = new BikeJourney();
		currentJourney.setStartDate( System.currentTimeMillis());
		currentJourney.setName(journeyName);
		
		journeys.add(currentJourney);
		
		tripStarted = true;
	
	}
	
	 private Location RouteLeg(Location start, Location end) { 
		 
	 	
	 	double distance = Point2PointDistance(start, end);
	 	double bearing  = Point2PointBearing(start, end);
	 	double x = (start.getLng() + end.getLng()) / 2;
	 	double y = (start.getLat() + end.getLat()) / 2;
	 	
	 	return new Location(x,y);
	 
		 	
	}
	
	 private double Point2PointDistance(Location pt1, Location pt2) { 
		 
		 double lat1 = pt1.getLat();
		 double lat2 = pt2.getLat();
		 double lon1 = pt1.getLng();
		 double lon2 = pt2.getLng();
		 double R = 6371000;
		 double dLat = Math.toRadians((lat2 - lat1));
		 double dLon = Math.toRadians((lon2 - lon1));
		 double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		 double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		 double d = R * c;
		 
		 return d;
	 }
		 
	 private double Point2PointBearing (Location pt1, Location pt2) { 
		double angle = 0;
		
	 	if (pt1 != null && pt2 != null && !pt1.equals(pt2)) { 
	 		double y1 = Math.toRadians(pt1.getLat());
	 		double x1 = Math.toRadians(pt1.getLng());
	 		double y2 = Math.toRadians(pt2.getLat());
	 		double x2 = Math.toRadians(pt2.getLng());
	 		double a = Math.sin(x1 - x2) * Math.cos(y2);
	 		double b = Math.cos(y1) * Math.sin(y2) - Math.sin(y1) * Math.cos(y2) * Math.cos(x1 - x2);
	 		angle = -(Math.atan2(a, b));
	 		if (angle < 0.0) { 
	 			angle += Math.PI * 2.0;
	 		} 
	 		angle = (angle * 180.0 / Math.PI); //parseInt!
	 	} 
	 	
		return angle;
	}
	
}
