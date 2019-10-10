package com.example.traceroutetest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RestClient {

	public TracerouteData getLocationForIp(String ip) throws IOException, JSONException {
		URL url = new URL("http://freegeoip.net/json/" + ip);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		InputStream is = null;

		try {
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoInput(true);

			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				is = conn.getInputStream();
				String response = readStringFromStream(is);
				JSONObject responseJSON = new JSONObject(response);
				TracerouteData data = new TracerouteData();
				data.setHost(ip);
				try {
					data.setLatitude(Float.parseFloat(responseJSON.getString("latitude")));
					data.setLongitude(Float.parseFloat(responseJSON.getString("longitude")));
				} catch (NumberFormatException e) {
					throw new IOException("Could not parse coordinates from JSON " + responseJSON);
				}
				return data;
			} else {
				throw new IOException("Request failed with response code code " + responseCode);
			}
		} finally {
			closeStream(is);
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static void copyStreams(InputStream is, OutputStream os) throws IOException {
		byte[] temp = new byte[8*1024];
		int read = 0;
		while ((read = is.read(temp)) != -1) {
			os.write(temp, 0 , read);
		}

		os.flush();
	}

	public static String readStringFromStream(InputStream is) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			copyStreams(bis, bos);
			byte[] data = bos.toByteArray();
			closeStream(bos);
			return new String(data, "UTF-8");
		} finally {
			closeStream(bos);
		}
	}

	public static void closeStream(InputStream is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (IOException e) {}
	}

	public static void closeStream(OutputStream is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (IOException e) {}
	}
}
