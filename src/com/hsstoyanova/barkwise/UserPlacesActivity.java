package com.hsstoyanova.barkwise;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.MyPlacesListAdapter;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.data.Tag;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.Profile;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class UserPlacesActivity extends AppCompatActivity{

	private ProgressDialog pDialog;
	private JSONArray tagsJson;
	private ArrayList<Tag> tags;
	private ListView placesList;
	private ImageView ivAdd;
	private MyPlacesListAdapter adapter;
	private int userId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_places);
		
		ivAdd = (ImageView) findViewById(R.id.ivAddTag);
		ivAdd.setVisibility(View.INVISIBLE);
		
		
		try
		{
			Intent i = getIntent();
			int receivedId = i.getIntExtra("id", -1);
			if(receivedId != -1)
			{
				userId = receivedId;
			}
			else
			{
				userId = CurrentData.user.id;
				ivAdd.setVisibility(View.INVISIBLE);
			}
		}
		catch(Exception e)
		{
			userId = CurrentData.user.id;
		}
		
		
		
		ivAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplicationContext(),AddTagActivity.class);
				startActivity(i);
			}
		});
		
		if(isNetworkAvailable())
		{
	        new LoadPlaces().execute();
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		tagsJson = null;
		tags = null;
		adapter = null;
		System.gc();
		super.onDestroy();
	}
	
	private class LoadPlaces extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(UserPlacesActivity.this);
			pDialog.setMessage("Loading data..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			RequestParams params = new RequestParams();
			params.put("userId", userId/*Integer.toString(CurrentData.user.id)*/);
			Request request = new Request(PhpFiles.getUserPlaces, Utils.RequestActions.POST.toString(), params);
			
			JSONObject json = request.response;
			
			if(json != null)
			{
				try {
					int success = json.getInt(Utils.REQUEST_RESULT);
					
					if (success == 1) 
					{
						tagsJson = json.getJSONArray("tags");
					
						if(tags == null)
						{
							tags = new ArrayList<Tag>();
						}
						else
						{
							tags.clear();
						}
					
						
						for (int i = 0; i < tagsJson.length(); i++) 
						{
							JSONObject jsonTag = tagsJson.getJSONObject(i);
							String idStr =  jsonTag.getString("id");
							double lng = jsonTag.getDouble("longtitude");
							double lat = jsonTag.getDouble("latitude");
							String address = jsonTag.getString("address");
							String dateStr = jsonTag.getString("date");
							String caption = jsonTag.getString("caption");
							
							
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
							
							Tag tag = new Tag(id, userId, lng, lat, dateStr, caption, address);
							
							tags.add(tag);
						}
						
						placesList = (ListView) findViewById(R.id.listView);
				        
				        if(tags != null)
				        {
				        	runOnUiThread(new Runnable() {
				        	     @Override
				        	     public void run() {

				        	//stuff that updates ui
				        	    	 adapter = new MyPlacesListAdapter(getApplicationContext(), R.layout.my_place_layout, tags);
							            placesList.setAdapter(adapter);
				        	    }
				        	});
				        	
				           
				            
				        	placesList.setOnItemClickListener(new OnItemClickListener() {
				            	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				            		Tag item = (Tag) parent.getItemAtPosition(position);
				            		//Create intent
				            		Intent intent = new Intent(getApplicationContext(), UserPlaceActivity.class);
				            		intent.putExtra("id", item.id);
				            		intent.putExtra("lng", item.longtitude);
				            		intent.putExtra("lat", item.latitude);
				            		intent.putExtra("addr", item.address);
				            		intent.putExtra("date", item.date.toString());
				            		intent.putExtra("caption", item.caption);

				            		//Start details activity
				            		startActivity(intent);
				            	}
				            });
				        }
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
}
