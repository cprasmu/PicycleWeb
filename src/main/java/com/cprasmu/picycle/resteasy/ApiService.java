package com.cprasmu.picycle.resteasy;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.cprasmu.picycle.MetricsService;
import com.cprasmu.picycle.model.BikeJourney;


@Path("/sampleservice")
public class ApiService {
	
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
	
	@POST
	@Path("/journeys")
	@Produces("application/json")
	@Consumes("application/json")
	public ArrayList<BikeJourney> journeyData(Object data){
		
		LinkedHashMap<String,Object> map= (LinkedHashMap<String,Object>)data;
		MetricsService.getInstance().getCurrentBikeJourney().setMetaData(map);
	
	    return MetricsService.getInstance().getJourneys();    
	    
	}
	//DeltaDistanceServlet
	//PulseServlet
	//ElevationRequest
	//PWMLoad
	//BikeLoad
	
	
	
	
	
	
}

