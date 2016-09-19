package com.hsstoyanova.barkwise;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bumptech.glide.Glide;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageActivity extends AppCompatActivity {

	private ProgressDialog pDialog;
	private String date = "";
	private String filePath = "";
	private int id = -1;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		
		date = getIntent().getStringExtra("date");
		filePath = getIntent().getStringExtra("filePath");
		id = getIntent().getIntExtra("id", -1);
		
        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(date);
        
        Glide
        .with(this)
        .load(filePath)
        .asBitmap()
        .atMost()
        .into((ImageView)findViewById(R.id.image))
        ;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.image_menu, menu);
	    return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(isNetworkAvailable())
		{
			if(item.getItemId() == R.id.delete)
			{
				if(id != -1)
				{
					new DeleteImage().execute();
				}
				
				Intent i = new Intent(getApplicationContext(),GalleryActivity.class);
				startActivity(i);

				
			}
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
	
		return super.onOptionsItemSelected(item);
	}
	
	private class DeleteImage extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ImageActivity.this);
			pDialog.setMessage("Deleting image..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			String name =getFileNameFromPath();
			
			if(name != null && !name.isEmpty())
			{
				RequestParams params = new RequestParams();
				params.put("id", Integer.toString(id));
				params.put("name", name);
				
				Request request = new Request(PhpFiles.deleteImage, Utils.RequestActions.POST.toString(), params);
				
				JSONObject json = request.response;
				
				if(json != null)
				{
					try {
						int success = json.getInt(Utils.REQUEST_RESULT);
						
						if (success == 1) 
						{
							debugMsg(Utils.Message.imageDeleted);
							Intent i  = new Intent(getApplicationContext(), GalleryActivity.class);
							startActivity(i);
							finish();
						} 
						else {
							debugMsg(Utils.ErrorMessage.somethingWentWrong);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
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
			
			return null;
		}
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once done
			pDialog.dismiss();
		}
	}
	
	private String getFileNameFromPath()
	{
		if(filePath != null && !filePath.isEmpty())
		{
			return filePath.substring(filePath.lastIndexOf('/') + 1);	
		}
		else
		{
			return "";
		}
	}
	
	@Override
	public void onBackPressed() {
		Intent i  = new Intent(getApplicationContext(), GalleryActivity.class);
		startActivity(i);
		finish();
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
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
}
