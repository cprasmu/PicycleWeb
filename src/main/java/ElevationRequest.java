

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.type.TypeReference;

import com.cprasmu.picycle.MetricsService;
import com.cprasmu.picycle.model.ElevationPoint;

public class ElevationRequest extends HttpServlet  {
	
	private static final String API_KEY 			= "AIzaSyDVyQlW4jGu8DKHMBRzKdXS1xSyhlk2jr4";
	private static final String ELEVATION_BASE_URL 	= "https://maps.googleapis.com/maps/api/elevation/json";
	

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	  response.setContentType("application/json");
	  
	  String journeyName = "" + request.getParameter("journeyName");
	  
	  String samples = request.getParameter("samples");
	  String path = request.getParameter("path");
	   
	  //URL elevationURL = new URL(ELEVATION_BASE_URL + "?path=" + path + "&samples="+samples+"&key=" + API_KEY );
	  URL elevationURL = new URL(ELEVATION_BASE_URL + "?path=" + path + "&samples="+samples);
	  
      URLConnection yc = elevationURL.openConnection();
      BufferedReader in = new BufferedReader( new InputStreamReader( yc.getInputStream()));
      String inputLine;
      PrintWriter out = response.getWriter();
      StringBuffer data=  new StringBuffer();
      
      while ((inputLine = in.readLine()) != null) {
          out.println(inputLine);
          data.append(inputLine);
      }
      
      ObjectMapper objectMapper = new ObjectMapper();
      
      LinkedHashMap<String,Object> results = objectMapper.readValue(data.toString(), LinkedHashMap.class);

      List<ElevationPoint> evelvationProfile = (List<ElevationPoint>) results.get("results");
      		
      MetricsService.getInstance().getCurrentBikeJourney().setEvelvationProfile(evelvationProfile);
      
      //elevation=93.7548828125, location={lat=50.57826, lng=-4.83982}, resolution=610.8129272460938}
      //results.get("results");
      
      in.close();
      out.close();
      MetricsService.getInstance().reset();
	  MetricsService.getInstance().start(journeyName);
	}

	
	
	
	
}
