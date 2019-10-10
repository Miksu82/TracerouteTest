package com.example.traceroutetest;

import android.util.Log;

public class TracerouteData {

	private String host;
	private float latitude;
	private float longitude;
	private String city;
	private int order;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public boolean hasCoordinates() {
		return latitude != 0 && longitude != 0;
	}
	
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public boolean isValidAndDifferent(String anotherIP) {
		if (anotherIP == null) {
			return false;
		}
		
		String[] parts = host.split("\\.");
		String[] anotherParts = anotherIP.split("\\.");
		if (anotherParts.length != 4) {
			return false;
		}
		
		return !parts[0].equals(anotherParts[0]) || !parts[1].equals(anotherParts[1]);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(latitude);
		result = prime * result + Float.floatToIntBits(longitude);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TracerouteData other = (TracerouteData) obj;
		if (Float.floatToIntBits(latitude) != Float
				.floatToIntBits(other.latitude))
			return false;
		if (Float.floatToIntBits(longitude) != Float
				.floatToIntBits(other.longitude))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "TracerouteData [host=" + host + ", latitude=" + latitude
				+ ", longitude=" + longitude + "]";
	}
	
}
