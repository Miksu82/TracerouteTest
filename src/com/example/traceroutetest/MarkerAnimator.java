package com.example.traceroutetest;

import android.os.Handler;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerAnimator {
	private boolean animate;
	private MarkerOptions marker;
	
	public MarkerAnimator(MarkerOptions marker) {
		this.marker = marker;
		animate = true;
	}
	public void animateMarker(final GoogleMap map) {
        final Handler handler = new Handler();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final int speed = 10;
        animate = true;
        
        Log.i(MainActivity.TAG,"Stargin to animate marker at " + marker.getPosition());
        handler.post(new Runnable() {
        	long frame = 1;
        	Marker aniMarker;
        	
            @Override
            public void run() {
                // Calculate progress using interpolator
               float v = interpolator.getInterpolation((float)((frame * speed) % 360) / 360.0f);
              
               if (aniMarker != null) {
            	   aniMarker.remove();
               }
               marker.rotation(v * 360.0f);          
               aniMarker = map.addMarker(marker);
               //Log.i(MainActivity.TAG, "Animating marker to rotate " + v*360.0f);
                // Repeat till progress is complete.
               frame++;
                if (animate) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                	aniMarker.remove();
                	marker.rotation(0);
                	map.addMarker(marker);
                }
            }
        });
    }
	
	public void stopAnimation() {
		animate = false;
	}
	public boolean isAnimating(MarkerOptions marker2) {
		return marker2.getPosition().equals(marker.getPosition());
	}
}
