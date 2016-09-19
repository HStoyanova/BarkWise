package com.hsstoyanova.barkwise;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity 
{
	
	private ImageButton imgBtnGallery, imgBtnPets, imgBtnLocations, imgBtnReminders;
	private int backButtonHits = 0;
	private int userId = -1;
	private ProgressDialog pDialog;
	private TextView tvName;
	private ImageView ivPic;


	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		imgBtnGallery = (ImageButton)findViewById(R.id.imageBtnGallery);
		imgBtnLocations = (ImageButton)findViewById(R.id.imageBtnLocations);
		imgBtnReminders = (ImageButton)findViewById(R.id.imageBtnReminders);
		imgBtnPets = (ImageButton)findViewById(R.id.imageBtnPets);
		tvName = (TextView) findViewById(R.id.textViewUserNameValue);
		ivPic = (ImageView) findViewById(R.id.imageViewProfilePic);
		
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
		
		imgBtnGallery.setOnClickListener(onClickGallery);
		imgBtnLocations.setOnClickListener(onClickLocations);
		imgBtnReminders.setOnClickListener(onClickReminders);
		imgBtnPets.setOnClickListener(onClickPets);		
		
		SetControlsByUser();
		
		if(isNetworkAvailable())
		{
			new LoadUserData().execute();	
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
		
		
	}
	
	private void SetControlsByUser()
	{
		if(userId != CurrentData.user.id)
		{
			imgBtnReminders.setVisibility(4);
			
			LinearLayout.LayoutParams paramsOld = (LinearLayout.LayoutParams)imgBtnLocations.getLayoutParams();
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(paramsOld);
			params.weight = 1.0f;
			params.gravity = Gravity.CENTER;

			imgBtnLocations.setLayoutParams(params);
			
			
			imgBtnLocations.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent i = new Intent(getApplicationContext(),UserPlacesActivity.class);
					i.putExtra("id", userId);
					startActivity(i);
					finish();
				}
			}); 
			
			imgBtnGallery.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent i = new Intent(getApplicationContext(),GalleryActivity.class);
					i.putExtra("id", userId);
					startActivity(i);
					finish();
				}
			});
			
			imgBtnPets.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent i = new Intent(getApplicationContext(),PetsActivity.class);
					i.putExtra("id", userId);
					startActivity(i);
					finish();
				}
			});
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		if(userId == CurrentData.user.id)
		{
		    inflater.inflate(R.menu.main_menu, menu);
		}
		else
		{
			inflater.inflate(R.menu.empty_menu, menu);
		}
		
	    return true;
	}
	
	
	OnClickListener onClickPets = new OnClickListener(){
		// TODO Auto-generated method stub
		
		public void onClick(View v) 
		{
			if(isNetworkAvailable())
			{
				Intent i = new Intent(getApplicationContext(),PetsActivity.class);
				startActivity(i);
				finish();
			}
			else
			{
				debugMsg(Utils.ErrorMessage.internetConnection);
			}
		}
	};
	
	OnClickListener onClickLocations = new OnClickListener(){
		// TODO Auto-generated method stub
		
		public void onClick(View v) 
		{
			if(isNetworkAvailable())
			{
				Intent i = new Intent(getApplicationContext(),AddTagActivity.class);
				startActivity(i);
				finish();
			}
			else
			{
				debugMsg(Utils.ErrorMessage.internetConnection);
			}
		}
	};
	
	OnClickListener onClickReminders = new OnClickListener(){
		// TODO Auto-generated method stub
		
		public void onClick(View v) 
		{
			if(isNetworkAvailable())
			{
				Intent i = new Intent(getApplicationContext(),RemindersActivity.class);
				startActivity(i);
				finish();
			}
			else
			{
				debugMsg(Utils.ErrorMessage.internetConnection);
			}
		}
	};
	
	OnClickListener onClickGallery = new OnClickListener(){
		// TODO Auto-generated method stub
		
		public void onClick(View v) 
		{
			if(isNetworkAvailable())
			{
				Intent i = new Intent(getApplicationContext(),GalleryActivity.class);
				startActivity(i);
				finish();
			}
			else
			{
				debugMsg(Utils.ErrorMessage.internetConnection);
			}
		}
	};
	
	private class LoadUserData extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ProfileActivity.this);
			pDialog.setMessage("Loading data..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			RequestParams params = new RequestParams();
			params.put("id", userId);
			
			Request request = new Request(PhpFiles.get_user_profile_data, Utils.RequestActions.POST.toString(), params);
			
			JSONObject json = request.response;
			
			if(json != null)
			{
				try {
					int success = json.getInt("success");
					
					if (success == 1) 
					{
						String name = json.getString("name");
						String path = json.getString("path");
						if(name != null && !name.isEmpty())
						{
							tvName.setText(name);
						}
						if(path != null && !path.isEmpty())
						{
							Glide
					        .with(getApplicationContext())
					        .load(path)
					        .asBitmap()
					        .atMost()
					        .into((ImageView) findViewById(R.id.imageViewProfilePic))
					        ;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			else
			{
				//debugMsg(Utils.ErrorMessage.incorrectLoginInput);
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
					SharedPreferences  settings = getSharedPreferences("UserData", 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.clear();
					editor.commit();
					finish();
				}
			
			}
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
		
		
		return super.onOptionsItemSelected(item);
	}

	public void showDialog(Activity activity) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    
	    builder.setTitle("Profile picture");
	    builder.setMessage("Choose profile picture");
	    builder.setPositiveButton("Upload", null);
	    builder.setNegativeButton("Default", null);
	    builder.show();
	}
	
	DialogInterface.OnClickListener onClickUploadImage = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			showFileChooser();
		}
	};
	
	private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), 1);
    }
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	@Override
	public void onBackPressed() {
		backButtonHits++;
		if(backButtonHits == 2)
		{
			finish();	
		}
		else
		{
			debugMsg(Utils.Message.warningGoBackButton);	
		}
	}
}
