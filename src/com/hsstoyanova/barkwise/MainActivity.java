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
import android.preference.PreferenceManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

	private EditText txtUsername, txtPassword;
	private TextView txtRegister;
	private Button btnLogin;
	ProgressDialog pDialog;
	static final String REQUEST_RESULT= "success"; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnLogin = (Button)findViewById(R.id.btnLogin);
		txtUsername = (EditText)findViewById(R.id.txtUsername);
		txtPassword = (EditText)findViewById(R.id.txtPassword);
		txtRegister = (TextView)findViewById(R.id.txtRegistration);
		
		btnLogin.setOnClickListener(onClickLogin);
		txtRegister.setOnClickListener(onClickRegister);

		SharedPreferences settings = getSharedPreferences("UserData", 0);
		String name = settings.getString("username", "");
		int id = settings.getInt("id", -1);
		String email = settings.getString("email", "");
		int genderId = settings.getInt("gender", -1);
		
		if(name != null && !name.isEmpty() && id != -1)
		{
			CurrentData.user.name = name;
			CurrentData.user.id = id;
			CurrentData.user.email = email;
			CurrentData.user.genderId = genderId;
			
			Intent i = new Intent(getApplicationContext(),ProfileActivity.class);
			startActivity(i);
			finish();
		}
	}
	
	OnClickListener onClickRegister = new OnClickListener(){
		// TODO Auto-generated method stub
		
		public void onClick(View v) 
		{
			if(isNetworkAvailable())
			{
				Intent i = new Intent(getApplicationContext(),RegisterActivity.class);
				startActivity(i);
				finish();
			}
			else
			{
				debugMsg(Utils.ErrorMessage.internetConnection);
			}
		}
	};
	
	
	OnClickListener onClickLogin = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{	
			if(isNetworkAvailable())
			{
				String name = txtUsername.getText().toString();
				String pass = txtPassword.getText().toString();
				if(!name.isEmpty() && !pass.isEmpty())
				{
					new Login().execute();
				}
				else
				{
					debugMsg(Utils.ErrorMessage.missingNameAndPass);
				}
			}
			else
			{
				debugMsg(Utils.ErrorMessage.internetConnection);
			}
		}	
	};

	private class Login extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Logging in..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			String username = txtUsername.getText().toString();
			String password = txtPassword.getText().toString();
			String hashedPassword = hashPassword(password); 
			
			RequestParams params = new RequestParams();
			params.put("username", username);
			params.put("password", hashedPassword);
			
			Request request = new Request(PhpFiles.login, Utils.RequestActions.POST.toString(), params);
			
			JSONObject json = request.response;
			
			if(json != null)
			{
				try {
					int success = json.getInt(REQUEST_RESULT);
					
					if (success == 1) 
					{
						JSONArray jsonArray = json.getJSONArray("users");
						if(jsonArray != null)
						{
							CurrentData.user.id = jsonArray.getJSONObject(0).getInt("id");	
							CurrentData.user.name = jsonArray.getJSONObject(0).getString("username");	
							CurrentData.user.email = jsonArray.getJSONObject(0).getString("email");	
							CurrentData.user.genderId = jsonArray.getJSONObject(0).getInt("genderId");	
							
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
					} 
					else 
					{
						debugMsg(Utils.ErrorMessage.incorrectLoginInput);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			else
			{
				debugMsg(Utils.ErrorMessage.incorrectLoginInput);
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
	        //convert the byte to hex format method 1
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
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}	
