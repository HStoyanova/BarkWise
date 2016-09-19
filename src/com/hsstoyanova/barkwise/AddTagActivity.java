package com.hsstoyanova.barkwise;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.common.Validation;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AddTagActivity extends FragmentActivity
{
	private LatLng latLng;
	private GoogleMap googleMap;
	ProgressDialog pDialog;
	private ImageView ivSaveTag;
	private EditText etCaption;
	private LocationManager locationManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locate_me);

		ivSaveTag = (ImageView) findViewById(R.id.ivSaveTag);
		ivSaveTag.setOnClickListener(addTagLocation);
		etCaption = (EditText) findViewById(R.id.etCaption);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if(isGpsAvailable())
		{
			setUpMapIfNeeded();	
		}
		else
		{
			debugMsg(Utils.ErrorMessage.turnOnGps);
		}
	}

	protected void onDestroy() 
	{
		googleMap = null;
		latLng = null;
		locationManager.removeUpdates(locListener);
		System.gc();
		super.onDestroy();
	};
	
	OnClickListener addTagLocation = new OnClickListener(){
		// TODO Auto-generated method stub
		
		public void onClick(View v) 
		{
			if(isNetworkAvailable())
			{
				String caption = etCaption.getText().toString();
				if(Validation.isInputTextValid(caption))
				{
					new AddTagLocation().execute();	
				}
				else
				{
					debugMsg(Utils.ErrorMessage.invalidCaption);
				}
				
			}
			else
			{
				debugMsg(Utils.ErrorMessage.internetConnection);
			}
		}
	};
	
	private void setUpMapIfNeeded() 
	{
		if (googleMap == null) 
		{
			SupportMapFragment fragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
			if(fragment != null)
			{
				googleMap = fragment.getMap();
			}	
			else
			{
				debugMsg(Utils.ErrorMessage.somethingWentWrong);
				return;
			}
				
			if (googleMap != null)
			{
				setUpMap();
			}
		}
	}
	
	LocationListener locListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location arg0) {
			latLng = new LatLng(arg0.getLatitude(), arg0.getLongitude());
			googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// Do nothing

		}

		@Override
		public void onProviderEnabled(String arg0) {
			// Do nothing

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// Do nothing

		}
	};
	
	private void setUpMap()
	{
		googleMap.setMyLocationEnabled(true);
		//LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = locListener;
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.empty_menu, menu);
	    super.onCreateOptionsMenu(menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return true;
	}
	
	private class AddTagLocation extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AddTagActivity.this);
			pDialog.setMessage("Adding tag..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{	
			double lng = 0.0;
			double lat = 0.0;
			
			String address="";
			String caption = etCaption.getText().toString();
			
			if(latLng == null)
			{
				debugMsg(Utils.ErrorMessage.somethingWentWrong);
				return null;
			}
			else
			{
				lng = latLng.longitude;
				lat = latLng.latitude;
				address = getAddress(lat, lng);
			}
			
			SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
		    Date now = new Date();
		    String date = sdfDate.format(now);
			
			RequestParams params = new RequestParams();
			params.put("userId", CurrentData.user.id);
			params.put("longtitude", lng);
			params.put("latitude", lat);
			params.put("address", address);
			params.put("caption", caption);
			params.put("date", date);
			
			Request request = new Request(PhpFiles.addTagLocation, Utils.RequestActions.POST.toString(),
					params);
			
			JSONObject json = request.response;
			
			try {
					if(json != null)
					{
						int success = json.getInt("success");
						
						if (success == 1) 
						{
							debugMsg("Tag successfully added!");
							Intent i = new Intent(getApplicationContext(),UserPlacesActivity.class);
							startActivity(i);
							finish();
						} 
						else 
						{
							String msg = json.getString("message");
							debugMsg(Utils.ErrorMessage.somethingWentWrong + " " + msg);
						}
					}
					else
					{
						debugMsg(Utils.ErrorMessage.somethingWentWrong);
					}
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
			return null;
		}
		
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once done
			pDialog.dismiss();
		}
	}
	
	private String getAddress(double lat, double lng)
	{
		Geocoder geocoder;
		List<Address> addresses;
		geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
		String result = "";
		String address = ""; 
		String city = "";
		String state = "";
		String country = "" ;
		String knownName = "";

		try {
			addresses = geocoder.getFromLocation(lat, lng, 1);
			address = addresses.get(0).getAddressLine(0); 
			city = addresses.get(0).getLocality();
			state = addresses.get(0).getAdminArea();
			country = addresses.get(0).getCountryName();
			knownName = addresses.get(0).getFeatureName();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return "";
		} 
		result = knownName + ", " + address + ", " + city + ", " + country;
		
		return result;
	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(getApplicationContext(), UserPlacesActivity.class);
		startActivity(i);;
		finish();
	}
	
	public void debugMsg(String msg) {
		final String str = msg;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG)
						.show();
			}
		});
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private boolean isGpsAvailable() {
		
		final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

		if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
		    return false;
		}
		return true;
	}
}
