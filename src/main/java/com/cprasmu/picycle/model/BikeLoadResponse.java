package com.cprasmu.picycle.model;

public class BikeLoadResponse {
	
	double load;
	boolean valid;
	int pwmLoad;
	
	public double getLoad() {
		return load;
	}
	public void setLoad(double load) {
		this.load = load;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public int getPwmLoad() {
		return pwmLoad;
	}
	public void setPwmLoad(int pwmLoad) {
		this.pwmLoad = pwmLoad;
	}
	
	
	
	
	
	
}
