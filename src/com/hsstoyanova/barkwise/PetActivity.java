package com.hsstoyanova.barkwise;

import org.json.JSONException;
import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.Utils;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PetActivity extends AppCompatActivity{

	private ProgressDialog pDialog;
	private TextView tvName, tvOwner, tvDob, tvChip, tvBreed, tvWeight;
	private ImageView ivDel, ivEdit;
	private String name = "";
	private String owner = "";
	private String dob =""; 
	private String chip = "";
	private String breed = "";
	private String weight = "";
	private String id = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pet);
		
		tvName = (TextView) findViewById(R.id.tvName);
		tvOwner = (TextView) findViewById(R.id.tvOwner);
		tvDob = (TextView) findViewById(R.id.tvDob);
		tvChip = (TextView) findViewById(R.id.tvChip);
		tvBreed = (TextView) findViewById(R.id.tvBreed);
		tvWeight = (TextView) findViewById(R.id.tvWeight);
		ivDel = (ImageView) findViewById(R.id.btnDelete);
		ivEdit = (ImageView) findViewById(R.id.imageViewEdit);
		
		Bundle extras = getIntent().getExtras();
		if(extras != null)
		{
			id = extras.getString("id");
			name = extras.getString("name");
			owner = extras.getString("owner");
			dob = extras.getString("dob");
			chip = extras.getString("chip");
			breed = extras.getString("breed");
			weight = Double.toString(extras.getDouble("weight"));
			
			tvName.setText(name);
			tvOwner.setText(owner);
			tvDob.setText(dob);
			tvWeight.setText(weight);
			tvChip.setText(chip);
			tvBreed.setText(breed);
			
		}
		
		ivDel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isNetworkAvailable())
				{

					new DeletePet().execute();
				}
				else
				{
					debugMsg(Utils.ErrorMessage.internetConnection);
				}
				
			}
		});
		
		ivEdit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isNetworkAvailable())
				{
					Intent i = new Intent(getApplicationContext(),EditPetProfileActivity.class);
					i.putExtra("id", id);
					i.putExtra("name", name);
					i.putExtra("dob", dob);
					i.putExtra("chip", chip);
					i.putExtra("breed", breed);
					i.putExtra("weight", weight);
					startActivity(i);
				}
				else
				{
					debugMsg(Utils.ErrorMessage.internetConnection);
				}
			}
		});
		
	}
	
	private class DeletePet extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(PetActivity.this);
			pDialog.setMessage("Deleting reminder..");
		
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
				Request request = new Request(PhpFiles.deletePet, Utils.RequestActions.POST.toString(), params);
				
				JSONObject json = request.response;
				if(json != null)
				{
					try {
						int success = json.getInt(Utils.REQUEST_RESULT);
						
						if (success == 1) 
						{
							debugMsg(Utils.Message.petDeleted);
							Intent i = new Intent(getApplicationContext(), PetsActivity.class);
							startActivity(i);
							finish();
						}
						else
						{
							debugMsg(Utils.ErrorMessage.somethingWentWrong);
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
	    Intent i = new Intent(getApplicationContext(), PetsActivity.class);
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
