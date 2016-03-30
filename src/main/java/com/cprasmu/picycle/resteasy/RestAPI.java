package com.cprasmu.picycle.resteasy;

import java.io.File;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.inject.Inject;

import com.cprasmu.picycle.PiCycleServer;
import com.cprasmu.picycle.model.*;
import com.cprasmu.picycle.model.gpx.TrkType;
import com.cprasmu.picycle.utils.Utils;

@Path("/rest")
public class RestAPI {
	@Context org.jboss.resteasy.spi.HttpResponse response;
	
	@Inject 
	PiCycleServer server;
	
	@GET
	@Path("/import/gpx/{filename}")
	@Produces("application/json")
	public List<TrkType> parseGPX(@PathParam("filename")String filename) {
	    return Utils.parseGPX(filename) ;
	    
	}
	
	@GET
	@Path("/bikeLocation")
	@Produces("application/json")
	public BikeStatus getBikeStatus(){
		return server.getBikeStatus();
	}
	
	@GET
	@Path("/list/files/gpx")
	@Produces("application/json")
	public List<File> getGPXFiles(){
	    return Utils.getGPXFiles() ;
	    
	}
}
