package com.cprasmu.picycle.model;


public class JourneyPoint {
	
	private double altitude;
	private double distance;
	private long time;
	private double cadence;
	private double speed;
	private double heartRate;
	private double power;
	private Location geoPoint;
	private double load;
	
	public double getLoad() {
		return load;
	}
	public void setLoad(double load) {
		this.load = load;
	}
	public Location getGeoPoint() {
		return geoPoint;
	}
	public void setGeoPoint(Location geoPoint) {
		this.geoPoint = geoPoint;
	}
	public double getAltitude() {
		return altitude;
	}
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public double getCadence() {
		return cadence;
	}
	public void setCadence(double cadence) {
		this.cadence = cadence;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getHeartRate() {
		return heartRate;
	}
	public void setHeartRate(double heartRate) {
		this.heartRate = heartRate;
	}
	public double getPower() {
		return power;
	}
	public void setPower(double power) {
		this.power = power;
	}
	
	public String toString(){
		
		return time + "," + distance + "," + speed + "," + cadence + "," + power + "," + altitude + "," + heartRate ;
	}
	
}
