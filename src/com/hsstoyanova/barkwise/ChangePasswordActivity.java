package com.hsstoyanova.barkwise;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.common.Validation;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ChangePasswordActivity extends AppCompatActivity
{
	private EditText txtOldPass, txtNewPass, txtConfirmPass;
	private String oldPass, newPass, confirmPass;
	private ImageView ivSave;
	ProgressDialog pDialog;
	static final String REQUEST_RESULT= "success"; 

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_pass);
		
		ivSave = (ImageView)findViewById(R.id.ivSave);
		txtOldPass = (EditText)findViewById(R.id.txtOldPass);
		txtNewPass = (EditText)findViewById(R.id.txtNewPass);
		txtConfirmPass = (EditText)findViewById(R.id.txtConfirmPass);
		
		ivSave.setOnClickListener(onClickChangePass);
	}
	
	OnClickListener onClickChangePass = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{	
			if(isNetworkAvailable())
			{
				oldPass = txtOldPass.getText().toString();
				newPass = txtNewPass.getText().toString();
				confirmPass = txtConfirmPass.getText().toString();
				
				if(oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty())
				{
					debugMsg(Utils.ErrorMessage.allFieldsAreRequired);
				}
				else
				{
					if(!newPass.equals(confirmPass))
					{
						debugMsg(Utils.ErrorMessage.passwordsNotMatching);
					}
					else
					{
						new ChangePassword().execute();	
					}
				}
			}
			else
			{
				debugMsg(Utils.ErrorMessage.internetConnection);
			}
			
			
		}	
	};

	private class ChangePassword extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ChangePasswordActivity.this);
			pDialog.setMessage("Changing password..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			String oldHashedPassword = hashPassword(oldPass); 
			String newHashedPassword = hashPassword(newPass);
			
			RequestParams params = new RequestParams();
			params.put("oldPass", oldHashedPassword);
			params.put("newPass", newHashedPassword);
			params.put("id", Integer.toString(CurrentData.user.id));
			
			Request request = new Request(PhpFiles.changePassword, Utils.RequestActions.POST.toString(),
					params);
			
			JSONObject json = request.response;
			
			try {
				if(json != null)
				{
					int success = json.getInt(REQUEST_RESULT);
					
					if (success == 1) 
					{
						debugMsg(Utils.Message.changedParrword);
						Intent i = new Intent(getApplicationContext(),ProfileActivity.class);
						startActivity(i);
						finish();
					} 
					else 
					{
						String message = json.getString("message");
						if(message.contains("incorrect"))
						{
							debugMsg(Utils.ErrorMessage.incorrectPassword);
						}
						else
						{
						    debugMsg(Utils.ErrorMessage.somethingWentWrong);	
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
	
	private String hashPassword(String input)
	{
		String result = "";
		
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(input.getBytes());
		        
	        byte byteData[] = md.digest();
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < byteData.length; i++) 
	        {
	         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        
	        result = sb.toString();
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
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
					Intent i = new Intent(getApplicationContext(),AddReminderActivity.class);
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
				
				case R.id.edit_profile:
				{
					// edit profile activity
					Intent i = new Intent(getApplicationContext(),EditProfileActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.my_places:
				{
					Intent i = new Intent(getApplicationContext(),UserPlacesActivity.class);
					startActivity(i);
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
				{
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
