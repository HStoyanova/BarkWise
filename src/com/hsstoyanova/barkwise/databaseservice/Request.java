package com.hsstoyanova.barkwise.databaseservice;

import java.io.InputStream;

import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.util.Log;
import cz.msebera.android.httpclient.Header;

public class Request {

	static InputStream is = null;
	public JSONObject response = null;
	static String json = "";
	private static final String GOOGLE_API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json/";
	
	// constructor
	public Request(String url, String method, RequestParams params) 
	{
		if (method == Utils.RequestActions.POST.toString()) 
		{
			Log.d("POSTTT", "POSTTTTT REQUEST");
			
			ServerConnection.post(url, params, new JsonHttpResponseHandler() 
			{
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject _response) 
				{
					// If the response is JSONObject instead of expected
					// JSONArray
					response = _response;
					Log.d("HANDLEr=====---", response.toString());
				}
			});
		}
		else if (method == "GET") 
		{
			ServerConnection.get(url, params, new JsonHttpResponseHandler() 
			{
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject _response) 
				{
					// If the response is JSONObject instead of expected
					// JSONArray
					response = _response;
					Log.d("handler", response.toString());
				}
			});
		}
	}

	public Request(){}
	
	public void GetNearByVetClinics(RequestParams params)
	{
		ServerConnection.get(GOOGLE_API_URL, params, new JsonHttpResponseHandler() 
		{
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject _response) 
			{
				// If the response is JSONObject instead of expected
				// JSONArray
				Log.d("-as-s-a-sas--as--as--a", GOOGLE_API_URL);
				Log.d("-as-s-a-sas--as--as--a", _response.toString());
				response = _response;
				Log.d("handler", response.toString());
			}
		}, true);
	}
	
}
