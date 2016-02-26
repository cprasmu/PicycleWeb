package com.cprasmu.picycle.model;

public class Location {

		private double lat;
		private double lng;
		
		public Location(double lattitude, double longitude){
			this.lat = lattitude;
			this.lng = longitude;
		}
		
		public double getLat() {
			return lat;
		}
		public void setLat(double lattitude) {
			this.lat = lattitude;
		}
		public double getLng() {
			return lng;
		}
		public void setLng(double longitude) {
			this.lng = longitude;
		}
		
		
}
