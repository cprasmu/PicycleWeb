package com.cprasmu.picycle.model;

import java.util.ArrayList;



public class Step {
	
	private StringValue distance;
	private StringValue duration;
	private Location end_location;
	private Location start_location;
	private Polyline polyline;
	private String travel_mode;
	private String encoded_lat_lngs;
	private ArrayList<Location> path;
	private ArrayList<Location> lat_lngs;
	private String instructions;
	private String maneuver;
	private Location start_point;
	private Location end_point;
	
	public StringValue getDistance() {
		return distance;
	}
	public void setDistance(StringValue distance) {
		this.distance = distance;
	}
	public StringValue getDuration() {
		return duration;
	}
	public void setDuration(StringValue duration) {
		this.duration = duration;
	}
	public Location getEnd_location() {
		return end_location;
	}
	public void setEnd_location(Location end_location) {
		this.end_location = end_location;
	}
	public Location getStart_location() {
		return start_location;
	}
	public void setStart_location(Location start_location) {
		this.start_location = start_location;
	}
	public Polyline getPolyline() {
		return polyline;
	}
	public void setPolyline(Polyline polyline) {
		this.polyline = polyline;
	}
	public String getTravel_mode() {
		return travel_mode;
	}
	public void setTravel_mode(String travel_mode) {
		this.travel_mode = travel_mode;
	}
	public String getEncoded_lat_lngs() {
		return encoded_lat_lngs;
	}
	public void setEncoded_lat_lngs(String encoded_lat_lngs) {
		this.encoded_lat_lngs = encoded_lat_lngs;
	}
	public ArrayList<Location> getPath() {
		return path;
	}
	public void setPath(ArrayList<Location> path) {
		this.path = path;
	}
	public ArrayList<Location> getLat_lngs() {
		return lat_lngs;
	}
	public void setLat_lngs(ArrayList<Location> lat_lngs) {
		this.lat_lngs = lat_lngs;
	}
	public String getInstructions() {
		return instructions;
	}
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	public String getManeuver() {
		return maneuver;
	}
	public void setManeuver(String maneuver) {
		this.maneuver = maneuver;
	}
	public Location getStart_point() {
		return start_point;
	}
	public void setStart_point(Location start_point) {
		this.start_point = start_point;
	}
	public Location getEnd_point() {
		return end_point;
	}
	public void setEnd_point(Location end_point) {
		this.end_point = end_point;
	}
	
	
	
	
}
