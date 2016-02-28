package com.cprasmu.picycle.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import com.cprasmu.picycle.model.chart.ChartSeries;
import com.cprasmu.picycle.model.chart.DataSet;


public class BikeJourney {
	
	private String name ;
	private long startDate;
	private long duration = 0;
	private ArrayList<JourneyPoint> data = new ArrayList<JourneyPoint>();
	private List<ElevationPoint> evelvationProfile;
	private LinkedHashMap<String,Object>metaData ;
	
	
	public LinkedHashMap<String, Object> getMetaData() {
		return metaData;
	}

	public void setMetaData(LinkedHashMap<String, Object> metaData) {
		this.metaData = metaData;
	}

	public List<ElevationPoint> getEvelvationProfile() {
		return evelvationProfile;
	}

	public void setEvelvationProfile(List<ElevationPoint> evelvationProfile) {
		this.evelvationProfile = evelvationProfile;
	}

	public BikeJourney(){
		
		this.name = "Default Journey";
		this.startDate = System.currentTimeMillis();
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getStartDate() {
		return startDate;
	}
	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public ArrayList<JourneyPoint> getData() {
		return data;
	}
	public void setData(ArrayList<JourneyPoint> data) {
		this.data = data;
	}
	
	
	public String toGPX() {
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" ?>\r\n<gpx xmlns=\"http://www.topografix.com/GPX/1/1\">\r\n<trk>\r\n");
		sb.append("<name>" + getName() + "</name>\r\n");
		sb.append("<desc>PiCycle Virtual Journey</desc>\r\n");
		sb.append("<time>"+fmt.format(new Date())+"</time>\r\n");
		sb.append("<trkseg>\r\n");
		
		//for (JourneyPoint jp:data) {
		for (int idx=0; idx<data.size(); idx++) {
			JourneyPoint jp = data.get(idx);
			
			sb.append("<trkpt lat=\"" + jp.getGeoPoint().getLat() + "\" lon=\"" + jp.getGeoPoint().getLng() +"\">");
			if (idx==0) {
				sb.append("<name>Start</name>");
			}
			sb.append("<desc>Cadence: "+ jp.getCadence() +"  Speed:" + jp.getSpeed() + " Altitude: "+ jp.getAltitude() + "</desc>" );
			sb.append("<ele>"+jp.getAltitude() +"</ele>");
			sb.append("<time>"+ fmt.format(new Date(jp.getTime()))+"</time>");
			sb.append("</trkpt>\r\n");
		}
		
		sb.append("</trkseg>\r\n</trk>\r\n</gpx>");
		
		return sb.toString();
		
	}
	
	public String toTCX() {
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		
		StringBuffer sb = new StringBuffer("<TrainingCenterDatabase xmlns=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd\">\r\n");
		sb.append("\t<Courses>\r\n");
		sb.append("\t\t<Course>\r\n");
		
		sb.append("\t\t\t<Name>" + getName() + "</Name>\r\n");
		sb.append("\t\t\t<Lap>\r\n");
		sb.append("\t\t\t<TotalTimeSeconds>" + data.get(data.size()-1).getTime() + "</TotalTimeSeconds>\r\n");
		sb.append("\t\t\t<DistanceMeters>" + data.get(data.size()-1).getTime() + "</DistanceMeters>\r\n");
		sb.append("\t\t\t<BeginPosition>\r\n");
		sb.append("\t\t\t\t<LatitudeDegrees>" + data.get(0).getGeoPoint().getLat() + "</LatitudeDegrees><LongitudeDegrees>" + data.get(0).getGeoPoint().getLng() + "</LongitudeDegrees>\r\n");
		sb.append("\t\t\t</BeginPosition>\r\n");
		sb.append("\t\t\t<EndPosition>\r\n");
		sb.append("\t\t\t\t<LatitudeDegrees>" + data.get(data.size()-1).getGeoPoint().getLat() + "</LatitudeDegrees><LongitudeDegrees>" + data.get(data.size()-1).getGeoPoint().getLng() + "</LongitudeDegrees>\r\n");
		sb.append("\t\t\t</EndPosition>\r\n");
		sb.append("\t\t<Intensity>\r\n");
		sb.append("\t\tActive\r\n");
		sb.append("\t\t</Intensity>\r\n");
		sb.append("\t\t<Cadence>\r\n");
		sb.append("\t\t0\r\n");
		sb.append("\t\t</Cadence>\r\n");
		sb.append("\t\t</Lap>\r\n");
		
		sb.append("\t\t<Track>\r\n");
		
		for (int idx=0; idx<data.size(); idx++) {
			JourneyPoint jp = data.get(idx);
			
			sb.append("\t\t\t<Trackpoint>\r\n");
			sb.append("\t\t\t<Time>"+ fmt.format(new Date(jp.getTime())) + "</Time>\r\n");
			sb.append("\t\t\t<Position>\r\n");
			sb.append("\t\t\t<LatitudeDegrees>" + jp.getGeoPoint().getLat() + "</LatitudeDegrees>\r\n");
			sb.append("\t\t\t<LongitudeDegrees>" + jp.getGeoPoint().getLng() +"</LongitudeDegrees>\r\n");
			sb.append("\t\t\t</Position>\r\n");
			sb.append("\t\t\t<AltitudeMeters>"+jp.getAltitude() +"</AltitudeMeters>\r\n");
			sb.append("\t\t\t<DistanceMeters>"+jp.getDistance() +"</DistanceMeters>\r\n");
			sb.append("\t\t\t</Trackpoint>\r\n");
		}
		
		sb.append("\t\t</Track>\r\n");
		sb.append("\t\t</Course>\r\n");
		sb.append("\t</Courses>\r\n");
		sb.append("</TrainingCenterDatabase>");
		return sb.toString();
	
	}
	
	public String toKML() {
		
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/kml/2.2 http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd\">\r\n");
		sb.append("<Document>\r\n");
		sb.append("<name>" + getName() + "</name>\r\n");
		sb.append("<description>PiCycle Virtual Journey</description>\r\n");
		sb.append("<Style id=\"picycleStyle\"><IconStyle><scale>0.5</scale><Icon><href>http://www.gpsies.com/images/milestone.png</href></Icon></IconStyle><LineStyle><colorMode>random</colorMode><width>5</width></LineStyle></Style><Style id=\"flag_n\"><IconStyle><scale>0.8</scale><Icon><href>http://www.gpsies.com/images/flag.png</href></Icon></IconStyle><LabelStyle><scale>0</scale></LabelStyle><BalloonStyle><text>$[description]</text> </BalloonStyle></Style><Style id=\"flag_h\"><IconStyle><scale>0.8</scale><Icon><href>http://www.gpsies.com/images/flag.png</href></Icon></IconStyle><LabelStyle><scale>1</scale></LabelStyle><BalloonStyle><text>$[description]</text></BalloonStyle></Style><StyleMap id=\"flag\"><Pair><key>normal</key><styleUrl>#flag_n</styleUrl></Pair><Pair><key>highlight</key><styleUrl>#flag_h</styleUrl></Pair></StyleMap>");
		sb.append("<Placemark>\r\n");
		sb.append("<name>" + getName() + "</name><visibility>1</visibility><open>1</open><Snippet maxLines=\"0\" />");
		sb.append("<description><![CDATA[Generated by PiCycle]]></description><styleUrl>#picycleStyle</styleUrl><MultiGeometry><LineString><extrude>true</extrude><tessellate>true</tessellate><coordinates>");
		for (int idx=0; idx<data.size(); idx++) {
			JourneyPoint jp = data.get(idx);
			sb.append(jp.getGeoPoint().getLng()  +"," + jp.getGeoPoint().getLat() + "," +jp.getAltitude() + "\r\n");
		}
		
	    sb.append("</coordinates></LineString></MultiGeometry></Placemark></Document></kml>");
		return sb.toString();
	}
	
	public String KMLLiveStart() {
		//TODO: generate this!
		String url= "http://192.168.1.73:8080/PiCycle/services/api/journeys/current/livekml";
		
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document><NetworkLink><Link><href>"+url+"</href><refreshMode>onExpire</refreshMode></Link></NetworkLink></Document></kml>");

		return sb.toString();
		
	}
	
	public String KMLLive() {
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		Date nowPlus1 =new Date(System.currentTimeMillis() + 1000);
		
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\"><NetworkLinkControl><expires>"+ fmt.format(nowPlus1) + "</expires></NetworkLinkControl>");
		sb.append("<Placemark><name>PiCycle</name><Point>");
		sb.append("<coordinates>" + data.get(data.size()-1).getGeoPoint().getLng() +"," + data.get(data.size()-1).getGeoPoint().getLat() + "," + data.get(data.size()-1).getAltitude() + "</coordinates>' % (lon,lat)");
		sb.append("</Point></Placemark></kml>");

		return sb.toString();
	}
	
	/*
	 * getSeries is used with HighCharts Javascript charts.
	{
    "xData": [],
    "datasets": [{
        "name": "Speed",
        "data": [],
        "unit": "km/h",
        "type": "line",
        "valueDecimals": 1
    }, {
        "name": "Elevation",
        "data": [],
        "unit": "m",
        "type": "area",
        "valueDecimals": 0
    }, {
        "name": "Heart rate",
        "data": [],
        "unit": "bpm",
        "type": "area",
        "valueDecimals": 0
    }]
}*/

	public DataSet getSeries(String callbackDunctionName) {

		ArrayList<Long> timeData 	= new ArrayList<Long>();
		ArrayList<Double> speedData = new ArrayList<Double>();
		ArrayList<Double> altData 	= new ArrayList<Double>();
		ArrayList<Double> powerData = new ArrayList<Double>();
		ArrayList<Double> heartData = new ArrayList<Double>();
		ArrayList<Double> cadenceData = new ArrayList<Double>();
		ArrayList<Double> loadData = new ArrayList<Double>();
		
		for (int idx=0; idx<data.size(); idx++) {
			
			JourneyPoint jp = data.get(idx);
			timeData.add(jp.getTime());
			speedData.add(jp.getSpeed());
			altData.add(jp.getAltitude());
			powerData.add(jp.getPower());
			heartData.add(jp.getHeartRate());
			cadenceData.add(jp.getCadence());
			loadData.add(jp.getLoad());
		}
		
		ChartSeries<Double> speedSeries 	= new ChartSeries<>(speedData,	"Speed",	"Km/H",	"line",1);
		ChartSeries<Double> altSeries 		= new ChartSeries<>(altData,	"Elevation","m",	"area",0);
		ChartSeries<Double> powerSeries 	= new ChartSeries<>(powerData,	"Power",	"Watts","area",0);
		ChartSeries<Double> heartSeries 	= new ChartSeries<>(heartData,	"Heart rate","Km/H","area",0);
		ChartSeries<Double> cadenceSeries 	= new ChartSeries<>(cadenceData,"Cadence",	"Rpm",	"area",0);
		ChartSeries<Double> loadSeries 		= new ChartSeries<>(loadData,	"Load",		"",		"line",1);
		
		DataSet<Long> dataset = new DataSet<Long>(timeData);
		
		dataset.getDatasets().add(speedSeries);
		dataset.getDatasets().add(altSeries);
		dataset.getDatasets().add(powerSeries);
		dataset.getDatasets().add(heartSeries);
		dataset.getDatasets().add(cadenceSeries);
		dataset.getDatasets().add(loadSeries);
		
		return dataset;
	}
}
