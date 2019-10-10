package com.example.traceroutetest;

import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AnalogClock;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
 
public class PolylineAnimator {
	
	private static boolean isAnimating = false;
	
    static void animateLine(final MapPoint point, final LatLngInterpolator latLngInterpolator, final AnimationStatusListener listener, final GoogleMap map) {
        isAnimating = true;
        final Polyline line = point.getLine();
        point.setShowing(true);
        final LatLng startPosition = line.getPoints().get(0);
        final LatLng finalPosition = line.getPoints().get(1);
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;
        final ArrayList<LatLng> points = new ArrayList<LatLng>();
        points.add(startPosition);
        Log.i(MainActivity.TAG, "Starting to animate line " + line.getPoints());
        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;
 
            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);
 
                LatLng prevPosition = null;
                if (points.size() == 2) {
                	prevPosition = points.remove(1);
                } else {
                	prevPosition = startPosition;
                }
                
                LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
                points.add(newPosition);
                line.setPoints(points);
                map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(newPosition).build()), 32, listener);
 
                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 32);
                } else {
                	isAnimating = false;
                	Log.i(MainActivity.TAG, "Finished animating line " + line.getPoints());
                	listener.animationFinished(point);
                	
                }
            }
        });
    }
        
    public static boolean isAnimating() {
    	return isAnimating;
    }
 
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    static void animateMarkerToHC(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
 
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = animation.getAnimatedFraction();
                LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
                marker.setPosition(newPosition);
            }
        });
        valueAnimator.setFloatValues(0, 1); // Ignored.
        valueAnimator.setDuration(3000);
        valueAnimator.start();
    }
 
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    static void animateMarkerToICS(Marker marker, LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(3000);
        animator.start();
    }
}
