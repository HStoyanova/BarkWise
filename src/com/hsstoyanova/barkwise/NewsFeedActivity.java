package com.hsstoyanova.barkwise;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.hsstoyanova.barkwise.common.CurrentData;

import com.hsstoyanova.barkwise.common.NewsFeedTagListAdapter;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.data.Tag;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;
import com.hsstoyanova.barkwise.common.Utils.*;

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
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class NewsFeedActivity extends AppCompatActivity{
	
	private ImageView ivAddTag;
	private ListView tagList;
	private ProgressDialog pDialog;
	private JSONArray jsonArrayRecentTags;
	private List<RecentTagInfoPair> recentTags;
	private NewsFeedTagListAdapter adapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_feed);
		
		ivAddTag = (ImageView) findViewById(R.id.ivAddTag);
		
		ivAddTag.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent i = new Intent(getApplicationContext(),AddTagActivity.class);
				startActivity(i);
				finish();
			}
		});

		if(isNetworkAvailable() && isGpsAvailable())
		{
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			LocationListener locationListener = new LocationListener() {

				@Override
				public void onLocationChanged(Location arg0) {
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
			
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
			
			if(CurrentData.latLng == null)
			{
				Location currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				
				if ( !locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER) ) {
			        debugMsg(Utils.ErrorMessage.turnOnGps);
				    Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
			        startActivity(i);
			        finish();
			    }
				
				if(currentLocation != null)
				{
					CurrentData.latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());	
				}
			} 
			new LoadNewsFeedInfo().execute();
		}
		else
		{
			debugMsg(Utils.ErrorMessage.turnOnGpsAndInternert);
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		jsonArrayRecentTags = null;
		recentTags = null;
		adapter = null;
		super.onDestroy();
	}
	
	private class LoadNewsFeedInfo extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(NewsFeedActivity.this);
			pDialog.setMessage("Loading data..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{	
			if(CurrentData.latLng == null)
			{
				debugMsg(Utils.ErrorMessage.couldnLocateYou);
				return null;
			}
			
			
				String latStr=Double.toString(CurrentData.latLng.latitude);
				String lngStr = Double.toString(CurrentData.latLng.longitude);
				
				if(latStr != null && latStr != "" && lngStr != null && lngStr != "")
				{
					RequestParams params = new RequestParams();
					params.put("userId", Integer.toString(CurrentData.user.id));
					params.put("radius", Integer.toString(Utils.RADIUS));
					params.put("fLat", Double.toString(CurrentData.latLng.latitude));
					params.put("fLon", Double.toString(CurrentData.latLng.longitude));
					
					Request request = new Request(PhpFiles.getAllRecentTags, Utils.RequestActions.POST.toString(), params);
					
					JSONObject json = request.response;
					
					if(json != null)
					{
						try {
							int success = json.getInt(Utils.REQUEST_RESULT);
							
							if (success == 1) 
							{
								jsonArrayRecentTags = json.getJSONArray("tags");
	
								if(jsonArrayRecentTags == null || jsonArrayRecentTags.length() == 0)
								{
									debugMsg(Utils.ErrorMessage.somethingWentWrong);
									return null;
								}
							
								recentTags = new ArrayList<RecentTagInfoPair>();
								
								for (int i = 0; i < jsonArrayRecentTags.length(); i++) 
								{
									JSONObject jsonTag = jsonArrayRecentTags.getJSONObject(i);
									String idStr =  jsonTag.getString("tagId");
									String userIdStr = jsonTag.getString("userId");
									double lng = jsonTag.getDouble("lon");
									double lat = jsonTag.getDouble("lat");
									String address = jsonTag.getString("addr");
									String dateStr = jsonTag.getString("date");
									String caption = jsonTag.getString("cap");
									String username = jsonTag.getString("user");
									
									int id = -1;
									if(idStr != null && idStr != "")
									{
										id = Integer.parseInt(idStr);
										
									}
									else
									{
										debugMsg(Utils.ErrorMessage.somethingWentWrong);
										return null;		
									}
									
									int userId = -1;
									if(userIdStr != null && userIdStr != "")
									{
										userId = Integer.parseInt(userIdStr);
										
									}
									else
									{
										debugMsg(Utils.ErrorMessage.somethingWentWrong);
										return null;	
									}
									
									
									Tag tag = new Tag(id, userId, lng, lat, dateStr, caption, address);
									
									RecentTagInfoPair rTag = new RecentTagInfoPair(tag, username); 
									
									recentTags.add(rTag);
									
								}
							
								tagList = (ListView) findViewById(R.id.listView);
						        
						        
						        	runOnUiThread(new Runnable() {
						        	     @Override
						        	     public void run() {
			
						        	//stuff that updates ui
						        	    	 adapter = new NewsFeedTagListAdapter(NewsFeedActivity.this, R.layout.user_tag_layout, recentTags);
						        	    	 tagList.setAdapter(adapter);
									            
						        	    }
						        	});
						        
						} 
						else {
							String message = json.getString("message");
							
							debugMsg(Utils.ErrorMessage.somethingWentWrong + message);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				else
				{
					debugMsg(Utils.ErrorMessage.somethingWentWrong);
				}
				
				}
				else
				{
					debugMsg(Utils.ErrorMessage.couldnLocateYou);
				}
			//}
			/*else
			{
				debugMsg(Utils.ErrorMessage.couldnLocateYou);
			}*/
				
			
		return null;
		}
		
		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
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
				
				case R.id.vet_clinics:
				{
					// vet clinics activity
					Intent i = new Intent(getApplicationContext(),VetsActivity.class);
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
				
				case R.id.my_places:
				{
					Intent i = new Intent(getApplicationContext(),UserPlacesActivity.class);
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
					Intent i = new Intent(getApplicationContext(),UserPlacesActivity.class);
					startActivity(i);
					break;
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
