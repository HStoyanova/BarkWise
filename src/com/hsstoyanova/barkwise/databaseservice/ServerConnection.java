package com.hsstoyanova.barkwise.databaseservice;

import java.net.URI;

import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.HttpGet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import android.util.Log;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;

public class ServerConnection {

	private static final String BASE_URL = "http://barkwise.freevar.com/bw_server_files/";
	
	//private static final String GOOGLE_API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=";
	private static final String GOOGLE_API_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
	
	  private static SyncHttpClient client = new SyncHttpClient();

	  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		  RequestHandle temp =client.get(formAbsoluteUrl(url), params, responseHandler);
		  
		  Log.d("ServerConnection", formAbsoluteUrl(url));
		  
		  if(temp == null)
		  {
			  Log.d("ServerConnection", "temp e null");
		  } 
		  else
		  {
			  Log.d("ServerConnection", "ne e nullll");
		  }
	  }

	  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		  Log.d("ERERERERERERE", url);
	
		  Log.d("URLLLLLLL", formAbsoluteUrl(url));
		  
		  RequestHandle temp = client.post(formAbsoluteUrl(url), params, responseHandler);
		  if(temp == null)
		  {
			  Log.d("AAAAAAAAAA", "AAAAAAAA");
		  }
		  
		  if(temp != null)
		  {
			  Log.d("OOOOOO", "ne e nullll");
		  }
	  }

	  private static String formAbsoluteUrl(String relativeUrl) {
	      return BASE_URL + relativeUrl;
	  }
	  
	  public static String formGoogleApiSearchByTextUrl(String lat, String lon, String type, String radiusInMeters)
	  {
		  return GOOGLE_API_URL + lat + "," + lon + "&radius=" + radiusInMeters + "&type=" + type + "&key=AIzaSyD48rZtcVkptpAitVr2H9sZcGJ6yfDF1ww";
	  }

	  
	  
	  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, boolean bool) {
		  
		  RequestHandle temp =client.get(GOOGLE_API_URL, params, responseHandler);
		  
		  Log.d("ServerConnection", GOOGLE_API_URL);
		  
		  if(temp == null)
		  {
			  Log.d("ServerConnection", "temp e null");
		  } 
		  else
		  {
			  Log.d("ServerConnection", "ne e nullll");
		  }
	  }

	  
}
