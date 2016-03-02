package com.cprasmu.picycle.resteasy;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.codehaus.jackson.map.ObjectMapper;

import com.cprasmu.picycle.MetricsService;
import com.cprasmu.picycle.Utils;
import com.cprasmu.picycle.model.BikeJourney;
import com.cprasmu.picycle.model.BikeLoadResponse;
import com.cprasmu.picycle.model.ConsumeDeltaResponse;
import com.cprasmu.picycle.model.ElevationPoint;
import com.cprasmu.picycle.model.Location;
import com.cprasmu.picycle.model.chart.DataSet;
import com.cprasmu.picycle.model.gpx.TrkType;


@Path("/api")
public class ApiService {
	@Context org.jboss.resteasy.spi.HttpResponse response;
	
	@GET
	@Path("/reset")
	@Produces("text/plain")
	public String reset(){
	    
	    MetricsService.getInstance().reset();
	    return "Reset";   
	}

	@GET
	@Path("/echo/{message}")
	@Produces("text/plain")
	public String echo(@PathParam("message")String message){
	    return message;    
	}
	
	@GET
	@Path("/journeys")
	@Produces("application/json")
	public ArrayList<BikeJourney> journeyInfo(){
		
	    return MetricsService.getInstance().getJourneys();    
	    
	}
	
	@GET
	@Path("/journeys/current/gpx")
	@Produces("application/gpx+xml")
	public String getJourneyGPX(){
		response.getOutputHeaders().putSingle("Content-Disposition", "inline; filename=\""+MetricsService.getInstance().getCurrentBikeJourney().getName()+".gpx\"");
	    return MetricsService.getInstance().getCurrentBikeJourney().toGPX();    
	    
	}
	
	@GET
	@Path("/journeys/current/tcx")
	@Produces("application/tcx+xml")
	public String getJourneyTCX(){
		response.getOutputHeaders().putSingle("Content-Disposition", "inline; filename=\""+MetricsService.getInstance().getCurrentBikeJourney().getName()+".tcx\"");
	    return MetricsService.getInstance().getCurrentBikeJourney().toTCX();    
	    
	}
	
	@GET
	@Path("/journeys/current/kml")
	@Produces("application/vnd.google-earth.kml+xml")
	public String getJourneyKML(){
		response.getOutputHeaders().putSingle("Content-Disposition", "inline; filename=\""+MetricsService.getInstance().getCurrentBikeJourney().getName()+".kml\"");
	    return MetricsService.getInstance().getCurrentBikeJourney().toKML();    
	    
	}
	
	@GET
	@Path("/journeys/current/livestart")
	@Produces("application/vnd.google-earth.kml+xml")
	public String getJourneyKMLLiveStart(){
	
	    return MetricsService.getInstance().getCurrentBikeJourney().KMLLiveStart();    
	    
	}
	
	@GET
	@Path("/journeys/current/livekml")
	@Produces("application/vnd.google-earth.kml+xml")
	public String getJourneyKMLLive(){
	    return MetricsService.getInstance().getCurrentBikeJourney().KMLLive();    
	    
	}
	
	@GET
	@Path("/journeys/current/chartseries")
	@Produces("application/json")
	public DataSet getChartSeries(){
	    return MetricsService.getInstance().getCurrentBikeJourney().getSeries("callbackDunctionName") ;
	    
	}
	
	@GET
	@Path("/import/gpx/{filename}")
	@Produces("application/json")
	public List<TrkType> parseGPX(@PathParam("filename")String filename){
	    return Utils.parseGPX(filename) ;
	    
	}
	
	@GET
	@Path("/list/files/gpx")
	@Produces("application/json")
	public List<File> getGPXFiles(){
	    return Utils.getGPXFiles() ;
	    
	}
	
	
	@POST
	@Path("/journeys")
	@Produces("application/json")
	@Consumes("application/json")
	
	public ArrayList<BikeJourney> journeyData(Object data){
		
		LinkedHashMap<String,Object> map= (LinkedHashMap<String,Object>)data;
		MetricsService.getInstance().getCurrentBikeJourney().setMetaData(map);
	
	    return MetricsService.getInstance().getJourneys();    
	    
	}
	
	
	@GET
	@Path("/consumeDelta/{lat}/{lng}")
	@Produces("application/json")
	public ConsumeDeltaResponse consumeDelta(@PathParam("lat")double lat,@PathParam("lng")double lng) {

	    return MetricsService.getInstance().consumeDelta(lat, lng);    
	}
	
	
	@GET
	@Path("/pulse")
	@Produces("application/json")
	public String pulse(){
		 for (int i =0; i<500; i++){
			 MetricsService.getInstance().wheelPulse();
			   try {
				   Thread.sleep(150);
			   } catch (InterruptedException e) {
				   e.printStackTrace();
			   }
		 }
	    
	    return "Reset";   
	}
	
	
	@GET
	@Path("/pwmLoad/{load}")
	@Produces("text/plain")
	public String pwmLoad(@PathParam("load")Integer load){
		MetricsService.getInstance().setPwmLoad(load);
	    return "Load set to : " + load;    
	}
	
	
	@GET
	@Path("/bikeLoad/{load}/{altitude}")
	@Produces("application/json")
	public BikeLoadResponse bikeLoad(@PathParam("altitude")double altitude,@PathParam("load")double load){

		MetricsService.getInstance().setAltitude(altitude);
		
	    return MetricsService.getInstance().setBikeLoad(load);  
	}
	
	@GET
	@Path("/elevation/{journeyName}/{samples}/{path}")
	@Produces("application/json")
	public List<ElevationPoint> elevation(@PathParam("journeyName") String journeyName, @PathParam("samples") int samples,@PathParam("path") String path) throws IOException{
		
		String API_KEY 				= "AIzaSyDVyQlW4jGu8DKHMBRzKdXS1xSyhlk2jr4";
		String ELEVATION_BASE_URL 	= "https://maps.googleapis.com/maps/api/elevation/json";
		
		URL elevationURL = new URL(ELEVATION_BASE_URL + "?path=" + path + "&samples=" + samples);
		URLConnection yc = elevationURL.openConnection();
		BufferedReader in = new BufferedReader( new InputStreamReader( yc.getInputStream()));
		
		String inputLine;
		StringBuffer data=  new StringBuffer();
		  
		while ((inputLine = in.readLine()) != null) {
			data.append(inputLine);
		}
		  
		ObjectMapper objectMapper = new ObjectMapper();
		  
		LinkedHashMap<String,Object> results = objectMapper.readValue(data.toString(), LinkedHashMap.class);
		
		List<ElevationPoint> evelvationProfile = (List<ElevationPoint>) results.get("results");
		  		
		MetricsService.getInstance().getCurrentBikeJourney().setEvelvationProfile(evelvationProfile);

		in.close();
		 
		MetricsService.getInstance().reset();
		MetricsService.getInstance().start(journeyName);
		
		return evelvationProfile;
	}
	
	@POST
	@Path("/decodePolyline/{poly}")
	@Produces("application/json")
	@Consumes("application/json")
	public List<Location> decodePolyline(String polyline){
		
	    return Utils.decodePoly(polyline);  
	}
}

