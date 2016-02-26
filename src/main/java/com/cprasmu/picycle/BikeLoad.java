package com.cprasmu.picycle;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



	public class BikeLoad extends HttpServlet {

		
		
		public void init() throws ServletException
		{
		   // Do required initialization
			 MetricsService.getInstance();
		  
		}

		public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		   // Set response content type
		   response.setContentType("application/json");
		   
		   
		   try{
			   double altitude = Double.parseDouble(request.getParameter("altitude").split(",")[1]);
		   		MetricsService.getInstance().setAltitude(altitude);
		   }catch(Exception nfe){
			   
		   }
		   // Actual logic goes here.
		   System.out.println(request.getParameter("load").toString());
		   MetricsService.getInstance().setBikeLoad(Float.parseFloat( request.getParameter("load").toString()));
		   
		   
		   PrintWriter out = response.getWriter();
		   out.println(request.getParameter("load").toString());
		//   out.println("{\"status\":200,\"count\":"+ MetricsService.getInstance().pulse() +",\"totalDistance\":"+MetricsService.getInstance().getTotalDistance() +"}");
		 
		  
		}

		public void destroy()
		{
		   // do nothing.
		}
	}
