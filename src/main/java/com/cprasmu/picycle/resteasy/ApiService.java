package com.cprasmu.picycle.resteasy;
import java.io.BufferedReader;
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
import com.cprasmu.picycle.model.BikeJourney;
import com.cprasmu.picycle.model.ConsumeDeltaResponse;
import com.cprasmu.picycle.model.ElevationPoint;


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
	@Path("/journeys/current")
	@Produces("application/gpx+xml")
	public String getJourneyGPX(){
		response.getOutputHeaders().putSingle("Content-Disposition", "inline; filename=\""+MetricsService.getInstance().getCurrentBikeJourney().getName()+".gpx\"");
	    return MetricsService.getInstance().getCurrentBikeJourney().toGPX();    
	    
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
	@Produces("text/plain")
	public String bikeLoad(@PathParam("altitude")double altitude,@PathParam("load")double load){
		
		MetricsService.getInstance().setAltitude(altitude);
		MetricsService.getInstance().setBikeLoad(load);
	    return "Load set to : " + altitude;    
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
	
	
}

