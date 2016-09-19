package com.hsstoyanova.barkwise;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.common.Utils.InfoPair;
import com.hsstoyanova.barkwise.common.AlarmReceiver;
import com.hsstoyanova.barkwise.data.ReminderType;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddReminderActivity extends AppCompatActivity{
	TimePicker TimePicker;
    DatePicker DatePicker;
    Button Setalarm;
    ProgressDialog pDialog;
    JSONArray jsonTypes;
    JSONArray jsonPets;
    Spinner spinnerTypes, spinnerPets;
    InfoPair selectedPet;
    EditText txtNote;
    int addedReminderId = 0;
    String requestCodeForAlarm = "";

	List<ReminderType> types;
	List<InfoPair> pets;

    ReminderType selectedReminderType;

    final static int RQS_1 = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_reminder_layout);
  
        DatePicker =(DatePicker)findViewById(R.id.datePicker1);
        TimePicker=(TimePicker)findViewById(R.id.timePicker1);
        spinnerTypes = (Spinner) findViewById(R.id.spinnerTypes);
        spinnerPets = (Spinner) findViewById(R.id.spinnerPets);
        Calendar now = Calendar.getInstance();
        txtNote = (EditText)findViewById(R.id.txtNote);
      
        DatePicker.init(
          now.get(Calendar.YEAR),
          now.get(Calendar.MONTH),
          now.get(Calendar.DAY_OF_MONTH),
          null);
       
        spinnerTypes.setOnItemSelectedListener(new OnItemSelectedListenerTypes());
        spinnerPets.setOnItemSelectedListener(new OnItemSelectedListenerPets());
        
        Setalarm = (Button) findViewById(R.id.Setalarm);
        Setalarm.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
            	
            	if(isNetworkAvailable())
        		{
            		Calendar current = Calendar.getInstance();
                    
                    Calendar cal = Calendar.getInstance();
                    cal.set(DatePicker.getYear(),
                            DatePicker.getMonth(),
                            DatePicker.getDayOfMonth(),
                      TimePicker.getCurrentHour(),
                      TimePicker.getCurrentMinute(),
                      00);
                    
                    if(cal.compareTo(current) <= 0)
                    {
                     //The set Date/Time already passed
                        Toast.makeText(getApplicationContext(),
                          "Invalid Date/Time",
                          Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                     //setAlarm(cal);
                     setAlarm(cal);setAlarm(cal);
                     new AddReminderInDB().execute();
                     
                    }
        		}
        		else
        		{
        			debugMsg(Utils.ErrorMessage.internetConnection);
        		}
        		
                   }});
    	
        new LoadRemindersAndUserPets().execute();
    }
   
	    private void setAlarm(Calendar targetCal) {
	    	
	        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
	        
	        if(selectedPet != null && selectedReminderType != null)
	        {
	        	String reminder = selectedReminderType.name;
		        String pet = selectedPet.name;
		        String note = txtNote.getText().toString();
		        if(reminder != null && reminder != "" &&
		        		pet != null && pet != "")
		        {
		        	if(note == null)
		        	{
		        		note = "No note for this reminder.";
		        	}
		        	intent.putExtra("reminder", reminder);;
		        	intent.putExtra("pet", pet);
		        	intent.putExtra("note", note);
		        }
		        
		        int currMilisec = (int)System.currentTimeMillis();
	        	
	        	requestCodeForAlarm = Long.toString(currMilisec);
		        
		        if (CurrentData.alarmManager == null)
		        {
		        	CurrentData.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);	
		        }
		        
		        PendingIntent pi = PendingIntent.getService(getApplicationContext(), currMilisec, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		        
		        CurrentData.alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(),pi);
		       
		        Intent intentReminders = new Intent(getApplicationContext(), RemindersActivity.class);
		        startActivity(intentReminders);
		        finish();
	        }
	    }
	    
	    private class AddReminderInDB extends AsyncTask<String, String, String>
		{
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				pDialog = new ProgressDialog(AddReminderActivity.this);
				pDialog.setMessage("Saving reminder..");
			
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.show();
			}
			
			protected String doInBackground(String... args) 
			{
				int day = DatePicker.getDayOfMonth();
			    int month = DatePicker.getMonth();
			    int year =  DatePicker.getYear();
			    int hours = TimePicker.getCurrentHour();
			    int minutes = TimePicker.getCurrentMinute();
			    
			    Calendar calendar = Calendar.getInstance();
			    calendar.set(year, month, day, hours, minutes);
			    Date date = calendar.getTime();
			    
			    String fDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			    String time = new SimpleDateFormat("hh:mm:ss").format(date);
			    
			    if(selectedPet != null && selectedReminderType != null && time != null && time != "" && CurrentData.user.id != -1 && requestCodeForAlarm != "")
			    {
			    	String note = txtNote.getText().toString();
			    	if(note == null)
			    	{
			    		note = "";
			    	}
			    		
			    	RequestParams params = new RequestParams();
					params.put("reminderId", Integer.toString(selectedReminderType.id));
					Log.d("reminder id",Integer.toString(selectedReminderType.id));
					params.put("petId", Integer.toString(selectedPet.id));
					Log.d("pet id", Integer.toString(selectedPet.id));
					params.put("userId", Integer.toString(CurrentData.user.id));
					Log.d("user id", Integer.toString(CurrentData.user.id));
					params.put("date", fDate);
					Log.d("date", fDate);
					params.put("time", time);
					Log.d("time", time);
					params.put("note", note);
					Log.d("note", note);
					params.put("rCode", requestCodeForAlarm);
					Log.d("note", note);
					Request request = new Request(PhpFiles.addReminder, Utils.RequestActions.POST.toString(), params);
					
					JSONObject json = request.response;
					
					if(json != null)
					{
						try {
							int success = json.getInt(Utils.REQUEST_RESULT);
						
							if (success == 1) 
							{
								debugMsg(Utils.Message.reminderAdded);
								addedReminderId = json.getInt("id");
							}
							else
							{
								debugMsg(Utils.ErrorMessage.somethingWentWrong);
							}
						}
						catch(Exception e)
						{
							debugMsg(Utils.ErrorMessage.somethingWentWrong);
						}
					}
					else
					{
						debugMsg(Utils.ErrorMessage.somethingWentWrong);
						Log.d("--TAG--", "json e null");
					}
			    }
			    else
			    {
			    	debugMsg(Utils.ErrorMessage.invalidReminderData);
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

	    private class LoadRemindersAndUserPets extends AsyncTask<String, String, String>
		{
	  		List<String> petsAsStr;
	  		List<String> typesAsStr;
	    	
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				pDialog = new ProgressDialog(AddReminderActivity.this);
				pDialog.setMessage("Loading data..");
			
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.show();
			}
			
			protected String doInBackground(String... args) 
			{
				RequestParams params = new RequestParams();
				params.put("ownerId", Integer.toString(CurrentData.user.id));
				Request request = new Request(PhpFiles.getUserPets, Utils.RequestActions.POST.toString(), params);
				
				JSONObject jsonObjPets = request.response;
				
				if(jsonObjPets != null)
				{
					try {
						int success = jsonObjPets.getInt(Utils.REQUEST_RESULT);
					
						if (success == 1) 
						{
							petsAsStr = new ArrayList<String>();
							jsonPets = jsonObjPets.getJSONArray("pets");

							if(pets == null)
							{
								pets = new ArrayList<InfoPair>();
							}

							for (int i = 0; i < jsonPets.length(); i++) 
							{
								JSONObject jsonPet = jsonPets.getJSONObject(i);
								String idStr =  jsonPet.getString("id"); 
								String name = jsonPet.getString("name");  
							
								int id= -1;
								if(idStr != null && idStr != "")
								{
									id = Integer.parseInt(idStr);
									
								}
								
								InfoPair pet = new InfoPair(id, name);
								pets.add(pet);
								petsAsStr.add(pet.name);
							}
							
							runOnUiThread(new Runnable() {
				        	     @Override
				        	     public void run() {

				        	    	 ArrayAdapter<String> adapter = new ArrayAdapter<String>(
												    getApplicationContext(), android.R.layout.simple_spinner_item, petsAsStr);
				        	    	 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				        	    	 spinnerPets.setAdapter(adapter);
				        	    }
				        	});
							
						} 
						else 
						{
							debugMsg(Utils.ErrorMessage.somethingWentWrong);
							return null;
						}
					} 
					catch (JSONException e) 
					{
						debugMsg(Utils.ErrorMessage.somethingWentWrong);
						return null;
					}
				}
				else
				{
					debugMsg(Utils.ErrorMessage.somethingWentWrong);
					return null;
				}

				Request requestTypes = new Request(PhpFiles.getReminderTypes, Utils.RequestActions.GET.toString(), null);
				JSONObject jsonObjTypes = requestTypes.response;
				
				if(jsonObjTypes != null)
				{
					try {
						int success = jsonObjTypes.getInt(Utils.REQUEST_RESULT);
						
						if (success == 1) 
						{
							jsonTypes = jsonObjTypes.getJSONArray("types");

							types = new ArrayList<ReminderType>();
							typesAsStr = new ArrayList<String>();
							
							for (int i = 0; i < jsonTypes.length(); i++) 
							{
								JSONObject jsonType = jsonTypes.getJSONObject(i);
								String idStr =  jsonType.getString("id");
								String name = jsonType.getString("name");
								
								int id = -1;
								if(idStr != null && idStr != "")
								{
									id = Integer.parseInt(idStr);
									
								}
								
								ReminderType type = new ReminderType(id, name);
								types.add(type);
								typesAsStr.add(type.name);
							}

							
							runOnUiThread(new Runnable() {
				        	     @Override
				        	     public void run() {

				        	//stuff that updates ui
				        	    	 ArrayAdapter<String> adapter = new ArrayAdapter<String>(
												    getApplicationContext(), android.R.layout.simple_spinner_item, typesAsStr);
				        	    	 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				        	    	 spinnerTypes.setAdapter(adapter);
				        	    }
				        	});
						} 
						else 
						{
							debugMsg(Utils.ErrorMessage.somethingWentWrong);
							return null;
						}
					} 
					catch (JSONException e) 
					{
						debugMsg(Utils.ErrorMessage.somethingWentWrong);
						return null;
					}
				}
				else
				{
					debugMsg(Utils.ErrorMessage.somethingWentWrong);
					return null;
				}
				
				return null;
			}
			
			protected void onPostExecute(String file_url) {
				// dismiss the dialog once done
				pDialog.dismiss();
			}
		}
	    
	    public class OnItemSelectedListenerTypes implements OnItemSelectedListener {

	        public void onItemSelected(AdapterView<?> parent,
	            View view, int pos, long id) {
	        	
	        	String selectedReminderStr = parent.getItemAtPosition(pos).toString();
	        	selectedReminderType = GetReminderByString(selectedReminderStr);
	        }

	        public void onNothingSelected(AdapterView parent) {
	          // Do nothing.
	        }
	    }
	    
	    public class OnItemSelectedListenerPets implements OnItemSelectedListener {

	        public void onItemSelected(AdapterView<?> parent,
	            View view, int pos, long id) {
	        	
	        	String selectedPetStr = parent.getItemAtPosition(pos).toString();
	        	selectedPet = GetPetByString(selectedPetStr);
	        }

	        public void onNothingSelected(AdapterView parent) {
	          // Do nothing.
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

	    public ReminderType GetReminderByString(String remStr)
	    {
	    	ReminderType result = null;
	    	if(types != null)
	    	{
	    		for (ReminderType rem : types) 
	    		{
					if(rem.name.equals(remStr))
					{
						result = rem;
						break;
					}
				}
	    	}
	    	
	    	return result;
	    }
	  
	    public InfoPair GetPetByString(String petStr)
	    {
	    	for(InfoPair pet: pets)
	    	{
	    		if(pet.name.equals(petStr))
	    		{
	    			return pet;
	    		}
	    	}
	    	
	    	return null;
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
		
		private boolean isNetworkAvailable() {
		    ConnectivityManager connectivityManager 
		          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		}
}

