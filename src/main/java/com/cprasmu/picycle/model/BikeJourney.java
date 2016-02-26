package com.cprasmu.picycle.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class BikeJourney {
	
	private String name ;
	private long startDate;
	private long duration = 0;
	private ArrayList<JourneyPoint> data = new ArrayList<JourneyPoint>();
	private List<ElevationPoint> evelvationProfile;
	private LinkedHashMap<String,Object>metaData ;
	
	
	public LinkedHashMap<String, Object> getMetaData() {
		return metaData;
	}

	public void setMetaData(LinkedHashMap<String, Object> metaData) {
		this.metaData = metaData;
	}

	public List<ElevationPoint> getEvelvationProfile() {
		return evelvationProfile;
	}

	public void setEvelvationProfile(List<ElevationPoint> evelvationProfile) {
		this.evelvationProfile = evelvationProfile;
	}

	public BikeJourney(){
		
		this.name = "Default Journey";
		this.startDate = System.currentTimeMillis();
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getStartDate() {
		return startDate;
	}
	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public ArrayList<JourneyPoint> getData() {
		return data;
	}
	public void setData(ArrayList<JourneyPoint> data) {
		this.data = data;
	}
	
	
	

}
