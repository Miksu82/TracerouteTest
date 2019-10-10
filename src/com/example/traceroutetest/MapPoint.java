package com.example.traceroutetest;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

public class MapPoint {
	Polyline line;
	MarkerOptions marker;
	boolean isShowing;
	
	public Polyline getLine() {
		return line;
	}
	public void setLine(Polyline line) {
		this.line = line;
	}
	public MarkerOptions getMarker() {
		return marker;
	}
	public void setMarker(MarkerOptions marker) {
		this.marker = marker;
	}
	
	public void remove() {
		line.remove();
		isShowing = false;
	}
	public boolean isShowing() {
		return isShowing;
	}
	public void setShowing(boolean isShowing) {
		line.setVisible(isShowing);
		this.isShowing = isShowing;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((line == null) ? 0 : line.hashCode());
		result = prime * result + ((marker == null) ? 0 : marker.hashCode());
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
		MapPoint other = (MapPoint) obj;
		if (line == null) {
			if (other.line != null)
				return false;
		} else if (!line.equals(other.line))
			return false;
		if (marker == null) {
			if (other.marker != null)
				return false;
		} else if (!marker.equals(other.marker))
			return false;
		return true;
	}
	
	
	
	
}
