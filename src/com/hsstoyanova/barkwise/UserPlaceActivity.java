package com.hsstoyanova.barkwise;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
//import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UserPlaceActivity extends FragmentActivity{

	private LatLng latLng;
	private GoogleMap googleMap;
	private ProgressDialog pDialog;
	private ImageView ivDelete;
	private TextView tvDate, tvCaption, tvAddr;
	private String addr, caption, date;
	private int id;
	private double lng,lat;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_place);

		setUpMapIfNeeded();
		
		ivDelete = (ImageView) findViewById(R.id.ivDelete);
		tvDate = (TextView) findViewById(R.id.tvDate);
		tvCaption = (TextView) findViewById(R.id.tvCaption);
		tvAddr = (TextView) findViewById(R.id.tvAddr);
		
		Intent intent = getIntent();
		id = intent.getIntExtra("id", -1);
		lng = intent.getDoubleExtra("lng", -1.0);
		lat = intent.getDoubleExtra("lat", -1.0);
		addr = intent.getStringExtra("addr");
		date = intent.getStringExtra("date");
		caption = intent.getStringExtra("caption");
		
		tvDate.setText(date);
		tvAddr.setText(addr);
		tvCaption.setText(caption);
		
		ivDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isNetworkAvailable())
				{
					new DeleteTag().execute();
				}
				else
				{
					debugMsg(Utils.ErrorMessage.internetConnection);
				}
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		googleMap = null;
		System.gc();
		super.onDestroy();
	}
	
	private void setUpMapIfNeeded() 
	{
		if (googleMap == null) 
		{
			SupportMapFragment fragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
			if(fragment != null)
			{
				googleMap = fragment.getMap();
			}	
				
			if (googleMap != null)
			{
				setUpMap();
			}
		}
	}
	
	private void setUpMap()
	{
		googleMap.setMyLocationEnabled(true);
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location arg0) {
				latLng = new LatLng(lat, lng);
				googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
				googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
				googleMap.addMarker(new MarkerOptions()
				        .position(latLng)
				        .title("You are here"));
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
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
	
	private class DeleteTag extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(UserPlaceActivity.this);
			pDialog.setMessage("Deleting tag..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		};
		
		protected String doInBackground(String... args) 
		{
			if(id != -1)
			{
				RequestParams params = new RequestParams();
				params.put("id", Integer.toString(id));
				Request request = new Request(PhpFiles.deleteTag, Utils.RequestActions.POST.toString(), params);
				
				JSONObject json = request.response;
				if(json != null)
				{
					try {
						int success = json.getInt(Utils.REQUEST_RESULT);
						
						if (success == 1) 
						{
							debugMsg(Utils.Message.tagDeleted);
							Intent i = new Intent(getApplicationContext(), UserPlacesActivity.class);
							startActivity(i);
							finish();
						}
						else
						{
							debugMsg(Utils.ErrorMessage.somethingWentWrong);
						}
					}
					catch(JSONException e)
					{
						debugMsg(Utils.ErrorMessage.somethingWentWrong);
					}
				}
				else
				{
					debugMsg(Utils.ErrorMessage.somethingWentWrong);
				}
			}
			
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
		super.onCreateOptionsMenu(menu);
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
					finish();
					break;
				}
			
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
	    Intent i = new Intent(getApplicationContext(), UserPlacesActivity.class);
	    startActivity(i);
	    finish();
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
}
