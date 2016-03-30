package com.cprasmu.picycle.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.cprasmu.picycle.dropBox.DropBox;
import com.cprasmu.picycle.model.Location;
import com.cprasmu.picycle.model.gpx.GpxType;
import com.cprasmu.picycle.model.gpx.RteType;
import com.cprasmu.picycle.model.gpx.TrkType;
import com.cprasmu.picycle.model.gpx.TrksegType;
import com.cprasmu.picycle.model.gpx.WptType;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DownloadErrorException;

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

	
	
	
	public static List<TrkType> parseGPX(String filename) {
		
		GpxType gpx = null;
		
	    try { 
	        JAXBContext jc = JAXBContext.newInstance("com.cprasmu.picycle.model.gpx");
	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        JAXBElement<GpxType> root = (JAXBElement<GpxType>)unmarshaller.unmarshal(new File("/home/pi/" + filename));
	        gpx = root.getValue();
	    } catch(JAXBException ex) {
	       System.out.println(ex);
	    }
	    
	    List<TrkType> tracks = gpx.getTrk();
	    
	    if (tracks.isEmpty()) {
	    	//if the track list is empty then try to build a track from a waypoint list
	    	List<RteType> routes = gpx.getRte();
	    	TrkType  t = new TrkType();
	  	    
	    	for (RteType route : routes) {
	    		  t.setName(route.getName());
	  	        System.out.println(route.getName());
	  	        List<WptType> waypoints = route.getRtept();
	  	       
	  	        TrksegType e = new TrksegType();
	  	        
	  	        for (WptType waypoint : waypoints) {
	  	        	e.getTrkpt().add(waypoint);
	  	        }
	  	        t.getTrkseg().add(e);
	  	    }
	    	tracks.add(t);
	    	
	    }
	    
	    return tracks;
	}
	
	public static List<TrkType> parseDBGPX(String filename) throws DownloadErrorException, DbxException {
		
		return  parseGPX(DropBox.getInputStreamForFile(filename)) ;
		
	}
	
	private static List<TrkType> parseGPX(InputStream is) {
		
		GpxType gpx = null;
		
	    try { 
	        JAXBContext jc = JAXBContext.newInstance("com.cprasmu.picycle.model.gpx");
	        
	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        JAXBElement<GpxType> root = (JAXBElement<GpxType>)unmarshaller.unmarshal(is);
	        gpx = root.getValue();
	    } catch(JAXBException ex) {
	       System.out.println(ex);
	    }
	    
	    List<TrkType> tracks = gpx.getTrk();
	
	    if (tracks.isEmpty()) {
	    	//if the track list is empty then try to build a track from a waypoint list
	    	List<RteType> routes = gpx.getRte();
	    	TrkType  t = new TrkType();
	  	    
	    	for (RteType route : routes) {
	    		  t.setName(route.getName());
	  	       // System.out.println(route.getName());
	  	        List<WptType> waypoints = route.getRtept();
	  	       
	  	        TrksegType e = new TrksegType();
	  	        
	  	        for (WptType waypoint : waypoints) {
	  	        	e.getTrkpt().add(waypoint);
	  	        }
	  	        t.getTrkseg().add(e);
	  	    }
	    	tracks.add(t);
	    	
	    }
	    
	    return tracks;
	}	
	
	public static ArrayList<File> getGPXFiles() {
		FileFilter filter = new FileFilter() {

		    @Override
		    public boolean accept(File file) {
		        if (file.isDirectory()) {
		            return true; // return directories for recursion
		        }
		        return file.getName().endsWith(".gpx"); // return .gpx files
		    }
		};
		
		File f =  new File("/home/pi/");
		
		return new ArrayList<File>(Arrays.asList(f.listFiles(filter)));
		
	}
	
}
