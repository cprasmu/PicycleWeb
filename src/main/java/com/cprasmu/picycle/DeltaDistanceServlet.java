package com.cprasmu.picycle;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class DeltaDistanceServlet extends HttpServlet{
	private String message;

	public void init() throws ServletException
	{
	   // Do required initialization
	 
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	   // Set response content type
	   response.setContentType("application/json");
	   try{
		   double lat = Double.parseDouble(request.getParameter("lat"));
		   double lng = Double.parseDouble(request.getParameter("lng"));
	   		
	   	   // Actual logic goes here.
	 	   PrintWriter out = response.getWriter();
	 	   out.println("{\"status\":200,\"time\":" + MetricsService.getInstance().getTripTime() + ",\"distance\":"+ MetricsService.getInstance().consumeDelta(lat,lng) + ",\"speed\":"+MetricsService.getInstance().getSpeedMPH() + ",\"cadence\":" + MetricsService.getInstance().getCadence() + ",\"totalDistance\":"+MetricsService.getInstance().getTotalDistance() +"}");
	 	
	   }catch(Exception nfe){
		   
	   }
	   
	}

	public void destroy()
	{
	   // do nothing.
	}
}





