package com.hsstoyanova.barkwise;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.common.Utils.OpenNowFlag;
import com.hsstoyanova.barkwise.common.VetsListAdapter;
import com.hsstoyanova.barkwise.data.VetClinic;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class VetsActivity extends AppCompatActivity{

	private ListView vetsListView;
	private VetsListAdapter adapter;
	private ProgressDialog pDialog;
	private List<VetClinic> vets;
	private String lat="";
	private String lon="";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vets);
		
		
		
		if(isNetworkAvailable() && isGpsAvailable())
		{
			updateCoordinates();
			new LoadVetsData().execute();
		}
		else
		{
			debugMsg(Utils.ErrorMessage.turnOnGpsAndInternert);
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		adapter = null;
		vets = null;
		System.gc();
		super.onDestroy();
	}
	
	private void updateCoordinates()
	{
		
			if(CurrentData.latLng != null)
			{
				lat = Double.toString(CurrentData.latLng.latitude);
				lon = Double.toString(CurrentData.latLng .longitude);
			}
			else
			{
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				LocationListener locationListener = new LocationListener() {

					@Override
					public void onLocationChanged(Location arg0) {
						lat = Double.toString(arg0.getLatitude());
						Log.d("VETSSSS lat", lat);
						lon = Double.toString(arg0.getLongitude());
						Log.d("VETSSSS lon", lon);
						CurrentData.latLng = new LatLng(arg0.getLatitude(), arg0.getLongitude());
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
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
				
				Location currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				
				if(currentLocation != null)
				{
					CurrentData.latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
					lat = Double.toString(currentLocation.getLatitude());
					Log.d("VETSSSS lat666", lat);
					lon = Double.toString(currentLocation.getLongitude());
					Log.d("VETSSSS lon66", lon);
				}
			}
	}
	
	
	private class LoadVetsData extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(VetsActivity.this);
			pDialog.setMessage("Loading data..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			RequestParams params = new RequestParams();
			
			
			params.put("location", lat+","+lon);
			params.put("radius", Utils.GOOGLE_SEARCH_RADIUS);
			params.put("query", Utils.VET);
			params.put("key", "AIzaSyDFJ-05GLC2VCCKUO2slcni0iaIf5PKOv8");
			
			Request request = new Request();
			request.GetNearByVetClinics(params);
			
			JSONObject json = request.response;
			
			if(json != null)
			{
				try {
					 JSONArray array =json.getJSONArray("results");
					 
					 if(vets == null)
					 {
						 vets = new ArrayList<VetClinic>();
					 }
					 
					 for (int i = 0; i < array.length(); i++) 
						{
							JSONObject jsonTag = array.getJSONObject(i);
							JSONObject geoObj = jsonTag.getJSONObject("geometry");
							JSONObject locObj =  geoObj.getJSONObject("location");
							String latRes = locObj.getString("lat");
							String lonRes = locObj.getString("lng");
							String iconUrl = jsonTag.getString("icon");
							String name = jsonTag.getString("name");
							String openNow = OpenNowFlag.NA;
							
							try
							{
								JSONObject hours = jsonTag.getJSONObject("opening_hours");
								boolean isOpenNow = hours.getBoolean("open_now");
								if(isOpenNow)
								{
									openNow = OpenNowFlag.YES;
								}
								else
								{
									openNow = OpenNowFlag.NO;
								}
								
							}
							catch(Exception e)
							{
								openNow = OpenNowFlag.NA;
							}
							
							VetClinic vet = new VetClinic(name, iconUrl, lonRes, latRes, openNow);
							vets.add(vet);
						}
					
					 vetsListView = (ListView) findViewById(R.id.listView);
					 if(vets != null)
					 {
						 runOnUiThread(new Runnable() {
			        	     @Override
			        	     public void run() {
			        	    	 adapter = new VetsListAdapter(getApplicationContext(), R.layout.vet_layout, vets);
			        	    	 vetsListView.setAdapter(adapter);
			        	    }
			        	});
						 
						 
						 vetsListView.setOnItemClickListener(new OnItemClickListener() {
				            	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				            		VetClinic item = (VetClinic) parent.getItemAtPosition(position);
				            		//Create intent
				            		Intent intent = new Intent(getApplicationContext(), VetActivity.class);
				            		intent.putExtra("lng", item.longtitude);
				            		intent.putExtra("lat", item.latitude);
				            		intent.putExtra("name", item.name);
				            		intent.putExtra("icon", item.icon);
				            		intent.putExtra("openNow", item.openNow);

				            		//Start details activity
				            		startActivity(intent);
				            	}
				            });
						 
					 }
				} catch (JSONException e) {
					debugMsg(Utils.ErrorMessage.somethingWentWrong +"1");
				}
			}
			else
			{
				debugMsg(Utils.ErrorMessage.somethingWentWrong+"2");
			}
			
			return null;
		}
		
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once done
			pDialog.dismiss();
		}
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		
		if(isNetworkAvailable())
		{
			switch(item.getItemId())	
			{
				case R.id.news_feed:
				{
					Intent i = new Intent(getApplicationContext(),NewsFeedActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.pets:
				{
					// pets activity
					Intent i = new Intent(getApplicationContext(),PetsActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				
				case R.id.gallery:
				{
					Intent i = new Intent(getApplicationContext(),GalleryActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.reminders:
				{
					// reminders activity
					Intent i = new Intent(getApplicationContext(),RemindersActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.my_places:
				{
					// vet clinics activity
					Intent i = new Intent(getApplicationContext(),UserPlacesActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.scan_chip:
				{
					// scan chip activity
					Intent i = new Intent(getApplicationContext(),ScanChipActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.suggestions:
				{
					Intent i = new Intent(getApplicationContext(),SuggestionsActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.edit_profile:
				{
					Intent i = new Intent(getApplicationContext(),EditProfileActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.change_pass:
				{
					Intent i = new Intent(getApplicationContext(),ChangePasswordActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.logout:
				{
				}
				
				default:
				{}
			
			}
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
	    Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
	    startActivity(i);
	    finish();
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
