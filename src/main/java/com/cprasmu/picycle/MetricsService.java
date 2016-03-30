package com.cprasmu.picycle;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.cprasmu.picycle.io.PWMDevice;
import com.cprasmu.picycle.model.*;

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
import com.pi4j.wiringpi.GpioUtil;

import javax.servlet.annotation.WebListener;


public class MetricsService  {

	private Float deltaDistance = new Float(0);
	private Float totalDistance = new Float(0);
	//private Float wheelDiameter = 2.08f;
	private Float wheelDiameter = 2.02f;
	private Float wheelCadence = new Float(0);
	
	private double 	cadence	 	= new Double(0);
	//private boolean tripStarted = false;
	private long 	lastWheelTime 	= 0;
	private double 	altitude 		= 0;
	//private long 	pulseCount 		= 0;
	private ArrayList<Float> speedList = new ArrayList<Float>(3);
	private boolean keepRunning 	= true;
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
	private double maxSpeed = 0;
	private double aveSpeed = 0;
			
	public static final int NOT_SET = Integer.MIN_VALUE;
	private long lastSpin, spinDiff,thisSpin=System.currentTimeMillis();
    //method timer variables
    private long timerStart, timerFinish, timerDiff, totalTimes, numTimes=0, maxTime;
    private final long MILLISECONDS_PM = 60000;
    private Timer sender  = null;
    
    private ArrayList<BikeJourney>journeys = new ArrayList<>();
    BikeJourney currentJourney = new BikeJourney();

	private PWMDevice.PWMChannel servo0;
	
	
	
	private class PulseCheckerTask extends TimerTask {
		
        public void run() {
        	if ((System.currentTimeMillis() - lastWheelTime)>2000 ) {
        		speedList.add(0f);
        		
				if (speedList.size()>3) {
					speedList.remove(0);
				}
        	}
        	
        	if ((System.currentTimeMillis() -lastSpin)>2000) {
        		cadence = 0d;
        		
        	}
        }
    }
	
	
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
	
	
	public BikeLoadResponse setBikeLoad(double load) {
		
		BikeLoadResponse response = new BikeLoadResponse();
		response.setLoad(load);
		
		if ((load < MIN_BIKE_LOAD) || (load > MAX_BIKE_LOAD)) {
			response.setValid(false);
			return response;
		}
		response.setValid(true);
		//TODO: refactor this to a request object?
		response.setPwmLoad(PWM_START + (int)((load-1) * PWM_MFACTOR));
		setPwmLoad(PWM_START + (int)((load-1) * PWM_MFACTOR));
		try {
			currentJourney.getData().get(currentJourney.getData().size()-1).setLoad(load);
		} catch(Exception ex) {
			System.out.println("Failed to set load for journeyPoint");
		}
		
		return response;
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
		wheelCadence  = new Float(0);
		lastWheelTime = System.currentTimeMillis();
		
	//	tripStarted = false;
		maxSpeed 	 = 0;
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
	
	private double calculatePowerOLD(double x){
		
		double result;

		double xx = x*x;
		double xxx = xx*x;
		double xxxx = xxx*x;
		double xxxxx = xxxx*x;
		double xxxxxx = xxxxx*x;
	
		result = -0.10129671505451972 - (30.233019453390963*x) + (5.1586949359111065*xx) - (0.286245217539615*xxx) + (0.007975447625589188*xxxx) -( 0.00010206945292925567*xxxxx) + (4.9894906712562e-7*xxxxxx);
		return result;		
		
	}
	
	private double calculatePower(double x){
		
		double result;

		double xx = x*x;
		double xxx = xx*x;
		double xxxx = xxx*x;
		double xxxxx = xxxx*x;
		double xxxxxx = xxxxx*x;
	
		//result = -0.10129671505451972 - (30.233019453390963*x) + (5.1586949359111065*xx) - (0.286245217539615*xxx) + (0.007975447625589188*xxxx) -( 0.00010206945292925567*xxxxx) + (4.9894906712562e-7*xxxxxx);
		
		result = 6.91062060056678 + (6.03692479349852*x) + (0.04503497878502449*xx) + (0.00006741516356990521*xxx);
		return result;		
		
	}
	
	public void finaliseJourney() {
		
		getCurrentBikeJourney().transferToDropBox();
		resetTrip();
		
	}
	
	private MetricsService() {
		
		
		//GpioUtil.enableNonPrivilegedAccess();
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
    	sender = new Timer("Pulse Checker",true); 
    	sender.schedule(new PulseCheckerTask(), 2000,2000);
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
	
	public synchronized void wheelPulse() {
		
	//	synchronized (deltaDistance) {
			
			
			//pulseCount++;
			
			long timeDiff = System.currentTimeMillis() - lastWheelTime;
			
			if (timeDiff>50) {
				deltaDistance += wheelDiameter;
				totalDistance += wheelDiameter;
				
				System.out.println("Speed : " + ((wheelDiameter/(timeDiff/1000f))*2.25) + ("\t" + timeDiff));
				
				wheelCadence = (float)(System.currentTimeMillis() - lastWheelTime)/1000f;
				lastWheelTime = System.currentTimeMillis();
				speedList.add((wheelDiameter/wheelCadence * 2.25f));
				
				if (speedList.size()>3) {
					speedList.remove(0);
				}
			} else {
				//invalid gate time
			}
			//return pulseCount;
	//	}
		
	}
	
	
	public double getSpeedMPH() {
		
		Float total=0f;
		double result= 0;
		
		if (speedList.size()==0) return 0;
		
		for (int i=0;i<speedList.size();i++) {
			total += speedList.get(i);
		}
		
		result = total/speedList.size();
		
		if (result>maxSpeed){
			maxSpeed = result;
		}

		return (total/speedList.size());
		
	}
	
	public double getMaxSpeedMPH() {
		
		return maxSpeed;
		
	}
	
	public double getAveSpeedMPH() {
		
		if (getTripTime()>0){
			
			// 10,000m in 1hour = 10Km/h
			
			// 10000 / 36000
		
			aveSpeed = (totalDistance * 0.6214) / (getTripTime() / 3600);
		
		} else {
			aveSpeed = 0;
		}
		
		
		return aveSpeed;
	}
	
	public synchronized void crankPulse() {
		
        //start timer
		if ((System.currentTimeMillis()-timerFinish)<100){
			System.out.println("IGNORING");
			return;
		}
        timerStart = System.currentTimeMillis();
       
       // double rpm = 0;
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
	
	/*
	public Float consumeDelta1(double lat,double lng) {
		
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
	*/
	
	public ConsumeDeltaResponse consumeDelta(double lat,double lng){

		ConsumeDeltaResponse response = new ConsumeDeltaResponse();
		
	//	synchronized (deltaDistance) {
			
			JourneyPoint jp = new JourneyPoint();
			jp.setTime(System.currentTimeMillis());
			jp.setDistance((double)deltaDistance);
			jp.setSpeed(getSpeedMPH());
			jp.setCadence(getCadence());
			jp.setPower(calculatePower(getSpeedMPH()*1.6));
			jp.setAltitude(altitude);
			jp.setGeoPoint(new Location(lat,lng));

			currentJourney.getData().add(jp);
			
			Float retval  = deltaDistance;
			
			
			if (!retval.isInfinite()){
				response.setDistance(deltaDistance);
			} else {
				response.setDistance(0d);
			}
			
			response.setTotalDistance(getTotalDistance());
			response.setCadence(getCadence());
			response.setSpeed(getSpeedMPH());
			response.setTime(getTripTime());
			response.setMaxSpeed(getMaxSpeedMPH());
			response.setAveSpeed(getAveSpeedMPH());
			response.setPower(jp.getPower());
			
			deltaDistance = 0f;
	//	}
		
		
		return response;
	}
	
	
	
	public BikeJourney getCurrentBikeJourney() {
		
		return currentJourney;
		
	}
	/*
	public static void writeFile(String line) {
		
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
	*/
	
	public double getTotalDistance() {
		
		return totalDistance;
	}
	
	public double getCadence() {
		
		return cadence;
		
	}
	
	public long getTripTime() {
		//TODO: return actual time and cal trip time in app,
		return System.currentTimeMillis() - currentJourney.getStartDate();
		
	}
	
	public void reset() {
		
		//pulseCount=0;
		deltaDistance = 0f;
		totalDistance = 0f;
	//	tripStarted = false;
		aveSpeed = 0;
		maxSpeed = 0;
	}
	
	public void start(String journeyName) {
		
		currentJourney = new BikeJourney();
		currentJourney.setStartDate( System.currentTimeMillis());
		currentJourney.setName(journeyName);
		
		journeys.add(currentJourney);
		
		//tripStarted = true;
	
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
