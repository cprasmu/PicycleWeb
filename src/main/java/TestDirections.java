import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;

import org.codehaus.jackson.map.ObjectMapper;


public class TestDirections {

	
		public static void main(String ... args) throws Exception{
			
			
			
			URL elevationURL = new URL("http://maps.googleapis.com/maps/api/directions/json?origin=bodmin&destination=wadebridge&sensor=false");
		      URLConnection yc = elevationURL.openConnection();
		      BufferedReader in = new BufferedReader( new InputStreamReader( yc.getInputStream()));
		      String inputLine;
		
		      StringBuffer data=  new StringBuffer();
		      
		      while ((inputLine = in.readLine()) != null) {
		      
		          data.append(inputLine);
		      }
		      
		      ObjectMapper objectMapper = new ObjectMapper();
		      
		      LinkedHashMap<String,Object> results = objectMapper.readValue(data.toString(), LinkedHashMap.class);
		      
		      System.out.println(results);
		      
		      
		      
		      
		}
}
