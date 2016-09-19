package com.hsstoyanova.barkwise;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.SuggestionsListAdapter;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.data.Suggestion;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class SuggestionsActivity extends AppCompatActivity{

	private ProgressDialog pDialog;
	private JSONArray jsonArr;
	private ArrayList<Suggestion> suggs;
	private ListView suggestionsList;
	private SuggestionsListAdapter adapter;
	private ImageView ivAdd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestions);
		
		ivAdd = (ImageView) findViewById(R.id.ivAdd);
		
		ivAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isNetworkAvailable())
				{
					Intent i = new Intent(getApplicationContext(),AddSuggestionActivity.class);
					startActivity(i);
					finish();	
				}
				else
				{
					debugMsg(Utils.ErrorMessage.internetConnection);
				}
			}
		});
		
		if(isNetworkAvailable())
		{
	        new LoadSuggestions().execute();
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		jsonArr = null;
		suggs = null;
		adapter = null;
		System.gc();
		super.onDestroy();
	}
	
	private class LoadSuggestions extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(SuggestionsActivity.this);
			pDialog.setMessage("Loading data..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			Request request = new Request(PhpFiles.getSuggestions, Utils.RequestActions.POST.toString(), null);
			
			JSONObject json = request.response;
			
			if(json != null)
			{
				try {
					int success = json.getInt(Utils.REQUEST_RESULT);
					
					if (success == 1) 
					{
						jsonArr = json.getJSONArray("suggs");

						if(suggs == null)
						{
							suggs = new ArrayList<Suggestion>();
						}
						
						for (int i = 0; i < jsonArr.length(); i++) 
						{
							JSONObject jsonTag = jsonArr.getJSONObject(i);
							String idStr =  jsonTag.getString("id");
							String user = jsonTag.getString("user");
							String descr = jsonTag.getString("descr");
							String date = jsonTag.getString("date");
							
							Suggestion sugg = new Suggestion(idStr, user, descr, date);
							suggs.add(sugg);
						}
						
						suggestionsList = (ListView) findViewById(R.id.listView);
				        
				        if(suggs != null)
				        {
				        	runOnUiThread(new Runnable() {
				        	     @Override
				        	     public void run() {
				        	    	 adapter = new SuggestionsListAdapter(getApplicationContext(), R.layout.activity_suggestion, suggs);
							            suggestionsList.setAdapter(adapter);
							            
				        	    }
				        	});
				        }
				        else
				        {
				        	debugMsg(Utils.ErrorMessage.noSuggestionsFound);
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
