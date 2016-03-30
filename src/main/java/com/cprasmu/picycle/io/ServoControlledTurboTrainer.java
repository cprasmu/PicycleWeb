package com.cprasmu.picycle.io;

import java.io.IOException;


public class ServoControlledTurboTrainer {
	
	private PWMDevice.PWMChannel loadServo;
	private static final int 	PWM_START = 150;
	private static final int 	PWM_MFACTOR = 60;
	private static final float 	MIN_BIKE_LOAD = 1.0f;
	private static final float 	DEFAULT_BIKE_LOAD = 3.0f;
	private static final float 	MAX_BIKE_LOAD = 5.0f;
	private static final int 	PWM_FREQ = 50;
	private int servoChan = 0;
	
	public ServoControlledTurboTrainer(int pwmChannel) throws IOException {
		
		this.servoChan = pwmChannel;
		initializeIO();
		setDefaultLoad();
		
	}
	
	private synchronized void initializeIO() throws IOException {
		
		PWMDevice device = new PWMDevice();
		device.setPWMFreqency(PWM_FREQ);
		loadServo = device.getChannel(servoChan);

	}
	
	
	public void setDefaultLoad() throws IOException {
		setBikeLoad(DEFAULT_BIKE_LOAD);
	}
	
	public void setBikeLoad(double load) throws IndexOutOfBoundsException, IOException {

		if ((load < MIN_BIKE_LOAD) || (load > MAX_BIKE_LOAD)) {
	
			throw new IndexOutOfBoundsException();
		}
		
		setPwmLoad(PWM_START + (int)((load-1) * PWM_MFACTOR));
	
	}
	
	public void setPwmLoad(int load) throws IOException{
			
		loadServo.setPWM(0,load);
		
	}
	
	public double calculatePower(double x){
		
		double result;

		double xx = x*x;
		double xxx = xx*x;
		double xxxx = xxx*x;
		double xxxxx = xxxx*x;
	//	double xxxxxx = xxxxx*x;
	
		//result = -0.10129671505451972 - (30.233019453390963*x) + (5.1586949359111065*xx) - (0.286245217539615*xxx) + (0.007975447625589188*xxxx) -( 0.00010206945292925567*xxxxx) + (4.9894906712562e-7*xxxxxx);
		
		result = 6.91062060056678 + (6.03692479349852*x) + (0.04503497878502449*xx) + (0.00006741516356990521*xxx);
		return result;		
		
	}
}
