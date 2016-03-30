package com.cprasmu.picycle.model;

public class BikeStatus {
	
	private long time;
	private double distance;
	private double speed;
	private double cadence;
	private double totalDistance;
	private double aveSpeed;
	private double maxSpeed;
	private double power;
	private Location location;
	
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public double getPower() {
		return power;
	}
	public void setPower(double power) {
		this.power = power;
	}
	public double getAveSpeed() {
		return aveSpeed;
	}
	public void setAveSpeed(double aveSpeed) {
		this.aveSpeed = aveSpeed;
	}
	public double getMaxSpeed() {
		return maxSpeed;
	}
	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getCadence() {
		return cadence;
	}
	public void setCadence(double cadence) {
		this.cadence = cadence;
	}
	public double getTotalDistance() {
		return totalDistance;
	}
	
	public void setTotalDistance(double totalDistance) {
		this.totalDistance = totalDistance;
	}
}
