package com.example.traceroutetest;

import com.google.android.gms.maps.GoogleMap.CancelableCallback;

public interface AnimationStatusListener extends CancelableCallback {

	public void animationFinished(MapPoint point);
}
