package com.hsstoyanova.barkwise;

import java.text.SimpleDateFormat;
import java.util.Date;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AddSuggestionActivity extends AppCompatActivity{

	private EditText etText;
	private ImageView ivSave;
	private ProgressDialog pDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_suggestion);
		
		etText = (EditText ) findViewById(R.id.etText);
		ivSave = (ImageView) findViewById(R.id.ivSave);
		
		ivSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String text = etText.getText().toString();
				if(!Validation.isInputTextValid(text))
				{
					debugMsg(Utils.ErrorMessage.noTextForSuggestion);
				}
				else if(isNetworkAvailable())
				{
					new AddSuggestion().execute();
				}
				else
				{
					debugMsg(Utils.ErrorMessage.internetConnection);
				}
			}
		});
	}
	
	
	private class AddSuggestion extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AddSuggestionActivity.this);
			pDialog.setMessage("Saving data..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{	
			String descr = etText.getText().toString();
			SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
		    Date now = new Date();
		    String date = sdfDate.format(now);
			
			RequestParams params = new RequestParams();
			params.put("userId", CurrentData.user.id);
			params.put("descr", descr);
			params.put("date", date);
			
			Request request = new Request(PhpFiles.addSuggestion, Utils.RequestActions.POST.toString(),
					params);
			
			JSONObject json = request.response;
			
			try {
				if(json != null)
				{
					int success = json.getInt("success");
					
					if (success == 1) 
					{
						debugMsg(Utils.Message.suggestionAdded);
						Intent i = new Intent(getApplicationContext(),SuggestionsActivity.class);
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
	public void onBackPressed() {
	    Intent i = new Intent(getApplicationContext(), SuggestionsActivity.class);
	    startActivity(i);
	    finish();
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
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
