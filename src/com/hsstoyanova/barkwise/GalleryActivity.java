package com.hsstoyanova.barkwise;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.common.GridViewAdapter;
import com.hsstoyanova.barkwise.data.Image;
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
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class GalleryActivity extends AppCompatActivity {

	private ProgressDialog pDialog;
	private JSONArray images;
	private int userId;
	private GridView gridView;
    private GridViewAdapter gridAdapter;
    private ArrayList<Image> imageItems;
	
    @Override 
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		
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
		
		if(isNetworkAvailable())
		{
	        new LoadGallery().execute();
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
	}
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	imageItems = null;
    	images = null;
    	gridView = null;
    	gridAdapter = null;
    	System.gc();
    }
    
	private class LoadGallery extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(GalleryActivity.this);
			pDialog.setMessage("Loading images..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			RequestParams params = new RequestParams();
			params.put("userId", userId);
			
			Request request = new Request(PhpFiles.getUserPictures, Utils.RequestActions.POST.toString(), params);
			
			JSONObject json = request.response;
			
			if(json != null)
			{
				try {
					int success = json.getInt(Utils.REQUEST_RESULT);
					
					if (success == 1) 
					{
						images = json.getJSONArray("images");

						if(images == null || images.length() == 0)
						{
							debugMsg(Utils.Message.noImages);
							return null;
						}
						
						if(imageItems == null)
						{
							imageItems = new ArrayList<Image>();
						}
						else
						{
							imageItems.clear();
						}
					
						for (int i = 0; i < images.length(); i++) 
						{
							JSONObject jsonImg = images.getJSONObject(i);
							String idStr =  jsonImg.getString("id");
							String url = jsonImg.getString("filePath");  
							String dateStr = jsonImg.getString("date");
							
							int id= -1;
							if(idStr != null && idStr != "")
							{
								id = Integer.parseInt(idStr);
								
							}
							else
							{
								debugMsg(Utils.ErrorMessage.somethingWentWrong);
								return null;
							}
							
							Image img = new Image(id, url, dateStr, userId, false);
							imageItems.add(img);
						}
						
						gridView = (GridView) findViewById(R.id.gridView);
				        
				        if(imageItems == null)
				        {
				        	debugMsg(Utils.Message.noImages);
				        	return null;
				        }
				        else
				        {
				        	runOnUiThread(new Runnable() {
				        	     @Override
				        	     public void run() {
				        	    	 gridAdapter = new GridViewAdapter(getApplicationContext(), R.layout.grid_item_layout, imageItems);
							            gridView.setAdapter(gridAdapter);   
				        	    }
				        	});
				            
				           gridView.setOnItemClickListener(new OnItemClickListener() {
				            	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				            		Image item = (Image) parent.getItemAtPosition(position);
				            		//Create intent
				            		Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
				            		intent.putExtra("date", item.uploadDate.toString());
				            		intent.putExtra("filePath", item.filePath);
				            		intent.putExtra("id", item.id);
				            		//Start details activity
				            		startActivity(intent);
				            	}
				            });
				        }
					} 
					else {
						debugMsg(Utils.ErrorMessage.somethingWentWrong);
						return null;
					}
				} catch (JSONException e) {
					e.printStackTrace();
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.gallery_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(isNetworkAvailable())
		{
			switch(item.getItemId())	
			{
				case R.id.upload:
				{
					Intent i = new Intent(getApplicationContext(),UploadImageActivity.class);
					startActivity(i);
					finish();
					return true;
				}
				
				case R.id.take_pic:
				{
					Intent i = new Intent(getApplicationContext(),CameraActivity.class);
					startActivity(i);
					finish();
					return true;
				}
			}
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
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
