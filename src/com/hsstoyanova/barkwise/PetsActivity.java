package com.hsstoyanova.barkwise;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.data.Breed;
import com.hsstoyanova.barkwise.data.Pet;
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

public class PetsActivity extends AppCompatActivity {
	
	private ProgressDialog pDialog;
	private JSONArray jsonPets;
	private ListView listView;
	private ImageView ivAdd;
    private ListViewPetsAdapter petsAdapter;
    private ArrayList<Pet> pets = new ArrayList<Pet>();
    private int userId = -1;
	
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pets);
		
		ivAdd = (ImageView)findViewById(R.id.ivAddPet);
		
		ivAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplicationContext(),AddPetActivity.class);
				startActivity(i);
			}
		});
		
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
			}
		}
		catch(Exception e)
		{
			userId = CurrentData.user.id;
		}
		if(isNetworkAvailable())
		{
			new LoadPets().execute();
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		jsonPets = null;
		petsAdapter = null;
		pets = null;
		System.gc();
		super.onDestroy();
	}
	
	private class LoadPets extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(PetsActivity.this);
			pDialog.setMessage("Loading data..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			
			RequestParams params = new RequestParams();
			params.put("ownerId", userId);
			Request request = new Request(PhpFiles.getUserPets, Utils.RequestActions.POST.toString(), params);
			
			JSONObject json = request.response;
			
			if(json != null)
			{
				try {
					int success = json.getInt(Utils.REQUEST_RESULT);
					
					if (success == 1) 
					{
						jsonPets = json.getJSONArray("pets");

						if(jsonPets == null || jsonPets.length() == 0)
						{
							debugMsg("images ne e null");
							Log.d("-----------------", "images e null ili empty");
						}
						
						if(pets == null)
						{
							pets = new ArrayList<Pet>();
						}
						else
						{
							pets.clear();
						}
					
						for (int i = 0; i < jsonPets.length(); i++) 
						{
							JSONObject jsonPet = jsonPets.getJSONObject(i);
							String idStr =  jsonPet.getString("id"); 
							String name = jsonPet.getString("name");  
							String breed =  jsonPet.getString("breed"); 
							Double weight =  jsonPet.getDouble("weight"); 
							String chipId = jsonPet.getString("chipId");  
							String userName = jsonPet.getString("user");
							String dob = jsonPet.getString("dob");
							
							int id= -1;
							if(idStr != null && idStr != "")
							{
								id = Integer.parseInt(idStr);
								
							}
							else
							{
								debugMsg(Utils.ErrorMessage.somethingWentWrong);	
								return null;
							}
							
							Pet pet = new Pet(id, name, userId, breed, weight, dob, userName,chipId);
							
							pets.add(pet);
						}
						
						listView = (ListView) findViewById(R.id.listView);
				        
				        if(pets != null)
				        {
				        	runOnUiThread(new Runnable() {
				        	     @Override
				        	     public void run() {

				        	    petsAdapter = new ListViewPetsAdapter(getApplicationContext(), R.layout.pet_layout, pets);
							            listView.setAdapter(petsAdapter); 
				        	    }
				        	});
				        	
				           listView.setOnItemClickListener(new OnItemClickListener() {
				            	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				            		Pet item = (Pet) parent.getItemAtPosition(position);
				            		//Create intent
				            		Intent intent = new Intent(getApplicationContext(), PetActivity.class);
				            		intent.putExtra("name", item.name);
				            		intent.putExtra("dob", item.dob);
				            		intent.putExtra("owner", item.userName);
				            		intent.putExtra("weight", item.weight);
				            		intent.putExtra("breed", item.breed);
				            		intent.putExtra("chip", item.chip);
				            		intent.putExtra("id", Integer.toString(item.id));
				            		
				            		//Start details activity
				            		startActivity(intent);
				            	}
				            });
				        }	
				        else
				        {
				        	debugMsg(Utils.ErrorMessage.noPetsAreFound);
				        }
					} 
					else 
					{
						debugMsg(Utils.ErrorMessage.noPetsAreFound);
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
