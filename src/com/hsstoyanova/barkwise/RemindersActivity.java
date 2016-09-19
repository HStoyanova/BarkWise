package com.hsstoyanova.barkwise;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.RemindersListAdapter;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.common.Utils.ReminderPair;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class RemindersActivity extends AppCompatActivity{

	private ProgressDialog pDialog;
	private JSONArray jsonArray;
	private ArrayList<ReminderPair> reminders;
	private ListView remindersList;
	private RemindersListAdapter adapter; 
	private ImageView btnAddReminder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminders);
		
		btnAddReminder = (ImageView) findViewById(R.id.ibtnAddReminder);
	    
		btnAddReminder.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplicationContext(),AddReminderActivity.class);
				startActivity(i);
				finish();
			}
		});
		if(isNetworkAvailable())
		{
	        new LoadReminders().execute();
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
	}

	private class LoadReminders extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(RemindersActivity.this);
			pDialog.setMessage("Loading data..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			RequestParams params = new RequestParams();
			params.put("userId", Integer.toString(CurrentData.user.id));
			Request request = new Request(PhpFiles.getAllReminders, Utils.RequestActions.POST.toString(), params);
			
			JSONObject json = request.response;
			
			if(json != null)
			{
				try {
					int success = json.getInt(Utils.REQUEST_RESULT);
					
					if (success == 1) 
					{
						jsonArray = json.getJSONArray("rems");

						if(jsonArray == null || jsonArray.length() == 0)
						{
							debugMsg(Utils.ErrorMessage.somethingWentWrong);
							return null;
						}
						
						if(reminders == null)
						{
							reminders = new ArrayList<ReminderPair>();
						}
						
						for (int i = 0; i < jsonArray.length(); i++) 
						{
							JSONObject jsonRem = jsonArray.getJSONObject(i);
							String idStr =  jsonRem.getString("id");
							String time = jsonRem.getString("time");
							String date = jsonRem.getString("date");
							String note = jsonRem.getString("note");
							String type = jsonRem.getString("type");
							String pet = jsonRem.getString("pet");
							String rCode = jsonRem.getString("rCode");
							
							Log.d("--------- json", rCode);
							
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
							
							Utils.ReminderPair rem = new Utils.ReminderPair(id,type, time, date, note, pet, rCode);
							
							reminders.add(rem);
						}
						
						remindersList = (ListView) findViewById(R.id.listView);
				        if(reminders != null)
				        {
				        	runOnUiThread(new Runnable() {
				        	     @Override
				        	     public void run() {

				        	//stuff that updates ui
				        	    	 adapter = new RemindersListAdapter(getApplicationContext(), R.layout.reminder_layout, reminders);
							            remindersList.setAdapter(adapter);
				        	    }
				        	});
				        	
				           
				        	remindersList.setOnItemClickListener(new OnItemClickListener() {
				            	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				            		ReminderPair item = (ReminderPair) parent.getItemAtPosition(position);
				            		//Create intent
				            		Intent intent = new Intent(getApplicationContext(), ReminderActivity.class);
				            		intent.putExtra("id", item.id);
				            		intent.putExtra("type", item.type);
				            		intent.putExtra("time", item.time);
				            		intent.putExtra("date", item.date);
				            		intent.putExtra("note", item.note);
				            		intent.putExtra("pet", item.pet);
				            		intent.putExtra("rCode", item.rCode);

				            		//Start details activity
				            		startActivity(intent);
				            	}
				            });
				        }
				        else
				        {
				        	debugMsg(Utils.ErrorMessage.noRemindersFound);
				        }
					} 
					else {
						String message = json.getString("message");
						debugMsg(Utils.ErrorMessage.somethingWentWrong + " " + message);
					}
				} catch (JSONException e) {
					debugMsg(Utils.ErrorMessage.somethingWentWrong);
				}
			}
			else
			{
				debugMsg(Utils.ErrorMessage.somethingWentWrong);
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
					break;
				}
				
				case R.id.pets:
				{
					// pets activity
					Intent i = new Intent(getApplicationContext(),PetsActivity.class);
					startActivity(i);
					break;
				}
				
				
				case R.id.gallery:
				{
					Intent i = new Intent(getApplicationContext(),GalleryActivity.class);
					startActivity(i);
					break;
				}
				
				case R.id.reminders:
				{
					// reminders activity
					Intent i = new Intent(getApplicationContext(),RemindersActivity.class);
					startActivity(i);
					break;
				}
				
				case R.id.vet_clinics:
				{
					// vet clinics activity
					Intent i = new Intent(getApplicationContext(),VetsActivity.class);
					startActivity(i);
					break;
				}
				
				case R.id.scan_chip:
				{
					// scan chip activity
					Intent i = new Intent(getApplicationContext(),ScanChipActivity.class);
					startActivity(i);
					break;
				}
				
				case R.id.suggestions:
				{
					Intent i = new Intent(getApplicationContext(),SuggestionsActivity.class);
					startActivity(i);
					break;
				}
				
				case R.id.edit_profile:
				{
					Intent i = new Intent(getApplicationContext(),EditProfileActivity.class);
					startActivity(i);
					break;
				}
				
				case R.id.my_places:
				{
					Intent i = new Intent(getApplicationContext(),UserPlacesActivity.class);
					startActivity(i);
					break;
				}
				
				case R.id.change_pass:
				{
					Intent i = new Intent(getApplicationContext(),ChangePasswordActivity.class);
					startActivity(i);
					break;
				}
				
				case R.id.logout:
				{
					Intent i = new Intent(getApplicationContext(),UserPlacesActivity.class);
					startActivity(i);
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
