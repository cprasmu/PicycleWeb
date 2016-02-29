package com.cprasmu.picycle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.cprasmu.picycle.model.Location;

public class Utils {

	
	
	public static List<Location> decodePoly(String encoded) {

	    List<Location> poly = new ArrayList<Location>();
	    int index = 0, len = encoded.length();
	    int lat = 0, lng = 0;

	    while (index < len) {
	        int b, shift = 0, result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;

	        shift = 0;
	        result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        Location p = new Location( (((double) lat / 1E5)),
	                 (((double) lng / 1E5) ));
	        poly.add(p);
	    }

	    return poly;
	}

	
	public static void parseGPX(String filename) {
		
		GpxType gpx = null;
		
	    try {
	        JAXBContext jc = JAXBContext.newInstance("topografix.gpx.schema11");
	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        JAXBElement<GpxType> root = (JAXBElement<GpxType>)unmarshaller
	            .unmarshal(new File(filename));
	        gpx = root.getValue();
	    } catch(JAXBException ex) {
	       // TODO
	    }

	    List<TrkType> tracks = gpx.getTrk();
	    for(TrkType track : tracks) {
	        System.out.println(track.getName());
	    }
	    
	}
	
}
