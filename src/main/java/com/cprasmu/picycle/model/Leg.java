package com.cprasmu.picycle.model;

import java.util.ArrayList;


public class Leg {
	
	private StringValue distance;
	private StringValue duration;
	private String end_address;
	private Location end_location;
	private String start_address;
	private Location start_location;
	private ArrayList<Step> steps;
	private ArrayList<Object> via_waypoint;
	private ArrayList<Object> via_waypoints;
	
}
