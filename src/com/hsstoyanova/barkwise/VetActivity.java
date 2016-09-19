package com.hsstoyanova.barkwise;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hsstoyanova.barkwise.common.VetsListAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class VetActivity extends FragmentActivity {

	private TextView tvName, tvOpenNow;
	private GoogleMap googleMap;
	private LatLng latLng;
	private String name= "";
	private String openNow = "";
	private String iconUrl = "";
	private String latStr = "";
	private String lngStr = "";
	private Double lat= -1.1;
	private Double lng = -1.1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vet);
		
		tvName = (TextView)findViewById(R.id.txtName);
		tvOpenNow = (TextView)findViewById(R.id.txIsOpenNow);
		
		Intent intent = getIntent();
		lngStr = intent.getStringExtra("lng");
		latStr = intent.getStringExtra("lat");
		name = intent.getStringExtra("name");
		openNow = intent.getStringExtra("openNow");
		iconUrl = intent.getStringExtra("icon");
		
		lat = Double.parseDouble(latStr);
		lng = Double.parseDouble(lngStr);
		
		tvName.setText(name);
		tvOpenNow.setText(openNow);
		
		setUpIcon();
		setUpMapIfNeeded();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		googleMap = null;
		latLng = null;
		System.gc();
		super.onDestroy();
	}
	
	private void setUpIcon()
	{
		if(iconUrl != "")
		{
			 Glide
		        .with(this)
		        .load(iconUrl)
		        .asBitmap()
		        .atMost()
		        .into((ImageView)findViewById(R.id.imageVet))
		        ;
		}
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
					        .title("Vet clinic"));
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
				LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
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
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
	    Intent i = new Intent(getApplicationContext(), VetsListAdapter.class);
	    startActivity(i);
	    finish();
	}
}
