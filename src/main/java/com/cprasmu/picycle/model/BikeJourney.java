package com.cprasmu.picycle.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
	
	
	public String toGPX() {
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" ?>\r\n<gpx xmlns=\"http://www.topografix.com/GPX/1/1\">\r\n<trk>\r\n");
		sb.append("<name>" + getName() + "</name>\r\n");
		sb.append("<trkseg>\r\n");
		
		for (JourneyPoint jp:data){
			sb.append("<trkpt lat=\"" + jp.getGeoPoint().getLat() + "\" lon=\"" + jp.getGeoPoint().getLng() +"\">");
			sb.append("<ele>"+jp.getAltitude() +"</ele>");
			sb.append("<time>"+ fmt.format(new Date(jp.getTime()))+"</time>");
			sb.append("</trkpt>\r\n");
		}
		
		sb.append("</trkseg>\r\n</trk>\r\n</gpx>");
		
		return sb.toString();
		
	}

}
