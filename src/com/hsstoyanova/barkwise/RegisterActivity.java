package com.hsstoyanova.barkwise;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
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
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity
{
	private EditText txtUsername, txtEmail, txtPassword, txtConfirmPassword;
	private ImageView ivRegister;
	ProgressDialog pDialog;
	String username, password, email; 
	private int gender = -1;
	
	static final String REQUEST_RESULT= "success"; // must be changed to Result(0/1) IN php too

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_user);
		
		ivRegister = (ImageView)findViewById(R.id.ivRegister);
		txtEmail = (EditText)findViewById(R.id.txtEmail);
		txtUsername = (EditText)findViewById(R.id.txtUsername);
		txtPassword = (EditText)findViewById(R.id.txtPassword);
		txtConfirmPassword = (EditText)findViewById(R.id.txtConfirmPassword);
		
		ivRegister.setOnClickListener(onClickRegister);
	}
	
	
	OnClickListener onClickRegister = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{	
			username = txtUsername.getText().toString();
			password = txtPassword.getText().toString();
			email = txtEmail.getText().toString();
			String confirmPassword = txtConfirmPassword.getText().toString();
			
			String resultFromValidation = Validation.IsInputUserDataValid(username, password, email, confirmPassword);
			
			if(resultFromValidation == Utils.ErrorMessage.validInput)
			{
				if(isNetworkAvailable())
				{
					new RegisterUser().execute();
				}
				else
				{
					debugMsg(Utils.ErrorMessage.internetConnection);
				}	
			}
			else
			{
				debugMsg(resultFromValidation);
			}
		}	
	};

	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private class RegisterUser extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(RegisterActivity.this);
			pDialog.setMessage("Creating account..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			String hashedPassword = hashPassword(password); 
			
			debugMsg("name:" + username + "email:" + email + "password:" + password);
			
			RequestParams params = new RequestParams();
			params.put("username", username);
			params.put("email", email);
			params.put("password", hashedPassword);
			
			if(gender != -1)
			{
				params.put("gender", Integer.toString(gender));	
			}
			
			Request request = new Request(PhpFiles.registerUser, Utils.RequestActions.POST.toString(),
					params);
			
			JSONObject json = request.response;
			
			try {
				if(json != null)
				{
					int success = json.getInt(REQUEST_RESULT);
					
					if (success == 1) 
					{
						int id = -1;
						String idStr = json.getString("userId");	
						try
						{
							id = Integer.parseInt(idStr);
						}
						catch(Exception e)
						{}
						
						debugMsg(Integer.toString(id));
						
						if(id != -1)
						{
							CurrentData.user.name = username;
							CurrentData.user.email = email;
							CurrentData.user.id = id;
							
							SharedPreferences  settings = getSharedPreferences("UserData", 0);
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("username", CurrentData.user.name);
							editor.putInt("id", CurrentData.user.id);
							editor.putString("email", CurrentData.user.email);
							editor.putInt("gender", CurrentData.user.genderId);
							editor.commit();
										
							Intent i = new Intent(getApplicationContext(),ProfileActivity.class);
							startActivity(i);
							finish();
						}
						else
						{
							debugMsg(Utils.ErrorMessage.somethingWentWrong);
						}
					} 
					else 
					{
						String mess = json.getString("message");
						Log.d("---------", mess);
						debugMsg(mess);
						debugMsg(Utils.ErrorMessage.existingUser);
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
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.empty_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return true;
	}
	
	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.radio_male:
	            if (checked)
	            {
	                gender = 0;
	            }
	            break;
	        case R.id.radio_female:
	            if (checked)
	            {
	            	gender =1;
	            }
	            break;
	    }
	}
	
}
