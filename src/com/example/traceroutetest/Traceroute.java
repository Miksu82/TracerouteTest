package com.example.traceroutetest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class Traceroute {

	private static final int MAX_PARTS = 4;

	private String mHost;
	private Context mContext;
	private File mTraceroute;
	private TracedataListener mListener;
	

	public Traceroute(Context context, String host, TracedataListener listener) {
		mHost = host;
		mContext = context;
		mListener = listener;

		mTraceroute = mContext.getFileStreamPath("traceroute");
		if (mTraceroute.exists()) {
			mTraceroute.delete();
		}

		try {
			InputStream is = mContext.getAssets().open("traceroute");
			FileOutputStream fos = new FileOutputStream(mTraceroute);
			RestClient.copyStreams(is, fos);
			is.close();
			fos.close();

			Runtime.getRuntime().exec("chmod 755 " + mTraceroute.getAbsolutePath());

		} catch (IOException e1) {
			Log.e(MainActivity.TAG, "Faile to copy assets", e1);
		}
	}

	public void start() {
		new TracerouteTask().execute();
	}

	private class TracerouteTask extends AsyncTask<Void, TracerouteData, List<TracerouteData>> {

		@Override
		protected List<TracerouteData> doInBackground(Void... params) {

			Log.d(MainActivity.TAG,"Starting to run process");
			Process p;
			List<TracerouteData> result = new ArrayList<TracerouteData>();
			RestClient client = new RestClient();

			try {
				InetAddress address = InetAddress.getByName(mHost);

				p = Runtime.getRuntime().exec(mTraceroute.getAbsolutePath() + " " + address.getHostAddress());
				InputStream is = p.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));

				String line = null;
				boolean isFirstLine = true;
				while ((line = reader.readLine()) != null) {
					Log.i(MainActivity.TAG, "Read line " + line);
					if (!isFirstLine) {
						String ip = parseIp(line.split(" "), 2);
						boolean shouldGetLocation = result.size() == 0 || result.get(result.size()-1).isValidAndDifferent(ip);
						//Log.i(MainActivity.TAG, "Found ip " + ip);
						if (shouldGetLocation) {
							TracerouteData data = client.getLocationForIp(ip);
							boolean shouldAddToList = result.size() == 0 || !result.get(result.size()-1).equals(data);
							if (shouldAddToList) {
								data.setOrder(result.size() + 1);
								publishProgress(data);
								result.add(data);
							}
						}
					}
					isFirstLine = false;
				}
				Log.i(MainActivity.TAG, "Trace finished");
			} catch (UnknownHostException e) {
				Log.e(MainActivity.TAG, "Failed to ip for " + mHost,e);
			} catch (IOException e) {
				Log.e(MainActivity.TAG, "Failed to get route",e);
			} catch (JSONException e) {
				Log.e(MainActivity.TAG, "Failed to parse json",e);
			}

			return result;
		}

		@Override
		protected void onProgressUpdate(TracerouteData... data) {
			mListener.newTracedata(data[0]);
		}

		@Override
		protected void onPostExecute(List<TracerouteData> result) {
			Log.i(MainActivity.TAG, "Found route " + result);
			mListener.traceFinished();
		}
	}

	private String parseIp(String[] line, int index) throws IOException {
		if (index > MAX_PARTS) {
			return null;
		}
		String ip = line[index];
		if (ip == null || ip.equals("") || ip.equals("*")) {
			return parseIp(line, ++index);
		}

		return ip;
	}
}
