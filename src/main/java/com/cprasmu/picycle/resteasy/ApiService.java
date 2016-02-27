package com.cprasmu.picycle.resteasy;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import com.cprasmu.picycle.MetricsService;
import com.cprasmu.picycle.model.BikeJourney;
import com.cprasmu.picycle.model.ConsumeDeltaResponse;


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
	
	
	//ElevationRequest

	
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
	
	
	
}

