package com.cprasmu.picycle;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Pulse extends HttpServlet {

	
	
	
	public void init() throws ServletException
	{
	   // Do required initialization
		 MetricsService.getInstance();
	  
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	   // Set response content type
	   response.setContentType("application/json");
	   
	   // Actual logic goes here.
	   PrintWriter out = response.getWriter();
	   
	   for(int i =0;i<500;i++){
	   
	  // out.println("{\"status\":200,\"count\":"+ MetricsService.getInstance().pulse() +",\"totalDistance\":"+MetricsService.getInstance().getTotalDistance() +"}");
	   out.println("{\"status\":200,\"count\":"+ MetricsService.getInstance().wheelPulse() + ",\"speed\":"+MetricsService.getInstance().getSpeedMPH() +",\"totalDistance\":"+MetricsService.getInstance().getTotalDistance() +"}");
	   
	   try {
		Thread.sleep(150);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   }
	}

	public void destroy()
	{
	   // do nothing.
	}
}
