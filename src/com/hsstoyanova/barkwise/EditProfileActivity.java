package com.hsstoyanova.barkwise;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView.ItemView;

import org.json.JSONException;
import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.common.Validation;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class EditProfileActivity extends AppCompatActivity{

	private EditText txtUsername, txtEmail;
	private ImageView ivSave;
	private RadioGroup radioGroupGender;
	ProgressDialog pDialog;
	String username, password, email; 
	private int gender = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);

		ivSave = (ImageView)findViewById(R.id.ivSave);
		txtEmail = (EditText)findViewById(R.id.txtEmail);
		txtUsername = (EditText)findViewById(R.id.txtUsername);
		radioGroupGender = (RadioGroup) findViewById(R.id.radioGroupGender);
		
		if(CurrentData.user != null)
		{
			txtEmail.setText(CurrentData.user.email);
			txtUsername.setText(CurrentData.user.name);
			
			if(CurrentData.user.genderId != -1)
			{
				if(CurrentData.user.genderId == 0)
				{
					radioGroupGender.check(R.id.radio_male);
					gender = 0;
				}
				else if(CurrentData.user.genderId == 1)
				{
					radioGroupGender.check(R.id.radio_female);
					gender = 1;
				}
				else
				{
					radioGroupGender.check(-1);
				}
					
			}
		}
			
		ivSave.setOnClickListener(onClickRegister);
	}

	OnClickListener onClickRegister = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{	
			if(isNetworkAvailable())
			{
				boolean validData = true;
				username = txtUsername.getText().toString();
				email = txtEmail.getText().toString();
				String valResult = Validation.IsInputUserDataValid(username, email);
				
				if(valResult == "")
				{
					new UpdateUserData().execute();	
				}
				else
				{
					debugMsg(valResult);	
				}
			}
			else
			{
				debugMsg(Utils.ErrorMessage.internetConnection);
			}
		}	
	};
	
	
	private class UpdateUserData extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(EditProfileActivity.this);
			pDialog.setMessage("Saving chnages..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			RequestParams params = new RequestParams();
			params.put("name", username);
			params.put("email", email);
			params.put("id", Integer.toString(CurrentData.user.id));
			
			if(gender != -1)
			{
				params.put("gender", gender);
			}
			
			Request request = new Request(PhpFiles.updateUser, Utils.RequestActions.POST.toString(),
					params);
			
			JSONObject json = request.response;
			
			try {
				if(json != null)
				{
					int success = json.getInt("success");
					
					if (success == 1) 
					{
						CurrentData.user.name = username;
						CurrentData.user.email = email;
						CurrentData.user.genderId = gender;
						
						debugMsg(Utils.Message.userDataUpdated);
						
						Intent i = new Intent(getApplicationContext(),ProfileActivity.class);
						startActivity(i);
						finish();
					} 
					else 
					{
						String message = json.getString("message");
						if(message.contains("went wrong"))
						{
							debugMsg(Utils.ErrorMessage.somethingWentWrong);
						}
						else
						{
							debugMsg(Utils.ErrorMessage.existingUser);	
						}
					}
				}
				else
				{
					debugMsg(Utils.ErrorMessage.somethingWentWrong);
				}
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
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
		debugMsg("v on create na menuto");
		
		getMenuInflater().inflate(R.menu.main_menu, menu);
		
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
					// suggestion activity
					Intent i = new Intent(getApplicationContext(),SuggestionsActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.change_pass:
				{
					// edit profile activity
					Intent i = new Intent(getApplicationContext(), ChangePasswordActivity.class);
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
				
				case R.id.logout:
				{
					SharedPreferences  settings = getSharedPreferences("UserData", 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.clear();
					editor.commit();
					finish();
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
	
	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.radio_male:
	            if (checked)
	            {
	            	Log.d("-------------", "female is checked");
	                gender = 0;
	            }
	            break;
	        case R.id.radio_female:
	            if (checked)
	            {
	            	Log.d("-------------", "female is checked");
	            	gender =1;
	            }
	            break;
	    }
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
