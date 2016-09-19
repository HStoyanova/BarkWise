package com.hsstoyanova.barkwise;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.common.Validation;
import com.hsstoyanova.barkwise.data.Breed;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
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
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class EditPetProfileActivity extends AppCompatActivity{
	ProgressDialog pDialog;
	
	private EditText tvName, tvDob, tvChip, tvWeight;
	private ImageView ivSave;
	Spinner spinnerBreeds;
	String name = "";
	String owner = "";
	String dob =""; 
	String chip = "";
	String breed = "";
	String weight = "";
	String id = "";
	ImageView btnAddReminder;
	Breed selectedBreed;
	List<Breed> breeds;
	JSONArray jsonBreeds;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_pet_profile);
		
		tvName = (EditText) findViewById(R.id.tvName);
		tvDob = (EditText) findViewById(R.id.tvDob);
		tvChip = (EditText) findViewById(R.id.tvChip);
		tvWeight = (EditText) findViewById(R.id.tvWeight);
		spinnerBreeds = (Spinner) findViewById(R.id.spinnerBreed);
		ivSave = (ImageView) findViewById(R.id.imageViewSave);
		
		Bundle extras = getIntent().getExtras();
		if(extras != null)
		{
			id = extras.getString("id");
			name = extras.getString("name");
			owner = extras.getString("owner");
			dob = extras.getString("dob");
			chip = extras.getString("chip");
			breed = extras.getString("breed");
			weight = extras.getString("weight");
			
			tvName.setText(name);
			tvDob.setText(dob);
			tvWeight.setText(weight);
			tvChip.setText(chip);
			
			
		}
		
		spinnerBreeds.setOnItemSelectedListener(new OnItemSelectedListenerBreed());
		
		ivSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isNetworkAvailable())
				{
					name = tvName.getText().toString();
					dob = tvDob.getText().toString();
					chip = tvChip.getText().toString();
					weight = tvWeight.getText().toString();
					
					Double weightDouble = -1.0;
					try
					{
						weightDouble = Double.parseDouble(weight);	
					}
					catch(Exception e)
					{}
					
					if(Validation.isUserPetNameValid(name) && weightDouble > 0)
					{
						new UpdatePetData().execute();
					}
				}
				else
				{
					debugMsg(Utils.ErrorMessage.internetConnection);
				}
				
			}
		});
		
		tvDob.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View v) {
	            // TODO Auto-generated method stub
	            Calendar mcurrentDate=Calendar.getInstance();
	            int mYear=mcurrentDate.get(Calendar.YEAR);
	            int mMonth=mcurrentDate.get(Calendar.MONTH);
	            int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

	            DatePickerDialog mDatePicker=new DatePickerDialog(getApplicationContext(), new OnDateSetListener() {                  
	                public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
	                    // TODO Auto-generated method stub  
	       			 String newDob = Integer.toString(selectedyear) + "-" + Integer.toString(selectedmonth+1) + "-" + Integer.toString(selectedday);
	                 tvDob.setText(newDob);
	                }
	            },mYear, mMonth, mDay);
	            mDatePicker.setTitle("Select date");                
	            mDatePicker.show();  }
	    });
		
		if(isNetworkAvailable())
		{
			new LoadBreedsData().execute();	
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		selectedBreed = null;
		breeds = null;
		jsonBreeds = null;
		System.gc();
		super.onDestroy();
	}
	
	public class OnItemSelectedListenerBreed implements OnItemSelectedListener {

	        public void onItemSelected(AdapterView<?> parent,
	            View view, int pos, long id) {
	        	
	        	String selectedBreedStr = parent.getItemAtPosition(pos).toString();
	        	selectedBreed = getBreedByName(selectedBreedStr);
	        }

	        public void onNothingSelected(AdapterView parent) {
	          // Do nothing.
	        }
	    }
	
	private class LoadBreedsData extends AsyncTask<String, String, String>
		{
	  		List<String> breedsAsStr;
	  		
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				pDialog = new ProgressDialog(EditPetProfileActivity.this);
				pDialog.setMessage("Loading data..");
			
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.show();
			}
			
			protected String doInBackground(String... args) 
			{
				Request request = new Request(PhpFiles.getBreeds, Utils.RequestActions.GET.toString(), null);
				
				JSONObject jsonObj = request.response;
				
				if(jsonObj != null)
				{
					try {
						int success = jsonObj.getInt(Utils.REQUEST_RESULT);
					
						if (success == 1) 
						{
							breedsAsStr = new ArrayList<String>();
							jsonBreeds = jsonObj.getJSONArray("breeds");

							if(breeds == null)
							{
								breeds = new ArrayList<Breed>();
							}

							for (int i = 0; i < jsonBreeds.length(); i++) 
							{
								JSONObject jsonBreed = jsonBreeds.getJSONObject(i);
								String idStr =  jsonBreed.getString("id"); 
								String name = jsonBreed.getString("name");  
							
								int id= -1;
								if(idStr != null && idStr != "")
								{
									id = Integer.parseInt(idStr);
									
								}
								
								Breed breed = new Breed(id, name);
								breeds.add(breed);
								breedsAsStr.add(breed.name);
							}
							
							runOnUiThread(new Runnable() {
				        	     @Override
				        	     public void run() {

				        	    	 ArrayAdapter<String> adapter = new ArrayAdapter<String>(
												    getApplicationContext(), android.R.layout.simple_spinner_item, breedsAsStr);
				        	    	 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				        	    	 spinnerBreeds.setAdapter(adapter);
				        	    	 spinnerBreeds.setSelection(adapter.getPosition(breed));
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
				breedsAsStr = null;
				pDialog.dismiss();
			}
		}
		
	private class UpdatePetData extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(EditPetProfileActivity.this);
			pDialog.setMessage("Saving changes..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		};
		
		protected String doInBackground(String... args) 
		{
			if(id != "")
			{
				
				RequestParams params = new RequestParams();
				params.put("id", id);
				params.put("name", name);
				params.put("dob", dob);
				params.put("weight", weight);
				params.put("breedId", Integer.toString(selectedBreed.id));
				params.put("chip", chip);
				
				Request request = new Request(PhpFiles.updatePet, Utils.RequestActions.POST.toString(), params);
				
				Log.d("pet ACTIVITY DEL", request.toString());
				
				JSONObject json = request.response;
				if(json != null)
				{
					try {
						int success = json.getInt(Utils.REQUEST_RESULT);
						
						if (success == 1) 
						{
							debugMsg(Utils.Message.petDataUpdated);
						}
						else
						{
							String message = json.getString("message");
							if(message.contains("chip number"))
							{
								debugMsg(Utils.ErrorMessage.chipIdTaken);
							}
							else
							{
								debugMsg(Utils.ErrorMessage.somethingWentWrong);	
							}
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
	
	private Breed getBreedByName(String name)
	{
		for(Breed br: breeds)
    	{
    		if(br.name.equals(name))
    		{
    			return br;
    		}
    	}
    	
    	return null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.empty_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
		startActivity(i);;
		finish();
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
