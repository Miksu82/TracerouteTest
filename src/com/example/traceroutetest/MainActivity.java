package com.example.traceroutetest;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.traceroutetest.LatLngInterpolator.LinearFixed;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity implements TracedataListener, AnimationStatusListener {

	public static final String TAG = "TraceTest";

	private TracerouteData mPrevData;
	private boolean mIsTracing;
	MarkerOptions mStart;
	List<MapPoint> mLines;
	MarkerAnimator mAnimator;
	MarkerOptions mHome;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mLines = new ArrayList<MapPoint>();
		GoogleMap map = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);
		Location location = map.getMyLocation();
		if (location != null) {
			mHome = new MarkerOptions()
			.position(new LatLng(location.getLatitude(), location.getLongitude()))
			.title("Home");
			map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
		} else {
			LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			MyLocationListener listener = new MyLocationListener();
			try{
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, listener);
			}catch(IllegalArgumentException e){
				Log.e(TAG,"Provider is null! The device doesn't have a network location provider. ",e);
			}
		}

		((Button)findViewById(R.id.start_trace_button)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView text = (TextView)findViewById(R.id.host_name_field);

				for (MapPoint line : mLines) {
					line.remove();
				}
				mLines.clear();
				Traceroute route = new Traceroute(MainActivity.this, text.getText().toString(), MainActivity.this);
				route.start();
				mIsTracing = true;

				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(text.getWindowToken(), 0);

				if (mHome != null) {
					animateMarker(mHome);
				}
			} 

		});


	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.i(TAG, "Config changed " + newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public synchronized void newTracedata(TracerouteData data) {
		Log.i(TAG, "New trace " + data);
		GoogleMap map = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		if (data.hasCoordinates()) {
			if (mPrevData == null && mHome != null) {
				mPrevData = new TracerouteData();
				mPrevData.setLatitude((float)mHome.getPosition().latitude);
				mPrevData.setLongitude((float)mHome.getPosition().longitude);
			}
			if (mPrevData != null) {
				PolylineOptions rectOptions = new PolylineOptions()
				.add(new LatLng(mPrevData.getLatitude(), mPrevData.getLongitude()))
				.add(new LatLng(data.getLatitude(), data.getLongitude()))
				.color(Color.RED);

				MarkerOptions m = new MarkerOptions()
				.position(new LatLng(data.getLatitude(), data.getLongitude()))
				.title(Integer.toString(data.getOrder()));

				MapPoint point = new MapPoint();
				point.setLine(map.addPolyline(rectOptions));
				point.setMarker(m);
				point.setShowing(false);

				mLines.add(point);

				if (!PolylineAnimator.isAnimating()) {
					PolylineAnimator.animateLine(point, new LinearFixed(), this, map);
					if (mLines.size() > 1) {
						MarkerOptions marker = mLines.get(mLines.size() - 2).getMarker();
						animateMarker(marker);					
					}
				}
			} else {
				mStart = new MarkerOptions()
				.position(new LatLng(data.getLatitude(), data.getLongitude()))
				.title(Integer.toString(data.getOrder()));
				animateMarker(mStart);
			}
			mPrevData = data;
		}

	}


	@Override
	public synchronized void animationFinished(MapPoint point) {
		int nextIndex = mLines.indexOf(point) + 1;
		if (nextIndex < mLines.size()) {
			PolylineAnimator.animateLine(mLines.get(nextIndex), new LinearFixed(), this, ((MapFragment) getFragmentManager()
					.findFragmentById(R.id.map)).getMap());
			if (mLines.size() > 1) {
				MarkerOptions marker = mLines.get(nextIndex - 1).getMarker();
				animateMarker(marker);					
			}
		} else {
			if (mIsTracing) {
				animateMarker(point.getMarker());
			} else {
				Toast.makeText(this, "Trace finished", Toast.LENGTH_LONG).show();
				mAnimator.stopAnimation();
			}
		}
	}

	private void animateMarker(MarkerOptions marker) {
		boolean startNewAnimation = false;
		if (mAnimator == null) {
			mAnimator = new MarkerAnimator(marker);
			startNewAnimation = true;
		} else if (!mAnimator.isAnimating(marker)) {
			mAnimator.stopAnimation();
			mAnimator = new MarkerAnimator(marker);
			startNewAnimation = true;
		}

		if (startNewAnimation) {
			mAnimator.animateMarker(((MapFragment) getFragmentManager()
					.findFragmentById(R.id.map)).getMap());	
		}
	}


	@Override
	public synchronized void traceFinished() {
		mIsTracing = false;
		if (!PolylineAnimator.isAnimating()) {
			Toast.makeText(this, "Trace finished", Toast.LENGTH_LONG).show();
			mAnimator.stopAnimation();
		}
	}

	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.removeUpdates(this);
			mHome = new MarkerOptions()
			.position(new LatLng(location.getLatitude(), location.getLongitude()))
			.title("Home");
			if (mIsTracing || PolylineAnimator.isAnimating()) {
				GoogleMap map = ((MapFragment) getFragmentManager()
						.findFragmentById(R.id.map)).getMap();
				map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}
	}

	@Override
	public void onCancel() {
		Log.i(TAG,"Map animate cancel");

	}


	@Override
	public void onFinish() {
		Log.i(TAG,"Map animate finish");

	}
}
