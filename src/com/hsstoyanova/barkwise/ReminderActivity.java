package com.hsstoyanova.barkwise;

import android.app.AlarmManager;
import android.app.PendingIntent;
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

import com.hsstoyanova.barkwise.common.AlarmReceiver;
import java.util.concurrent.ExecutionException;
import org.json.JSONException;
import org.json.JSONObject;
import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

public class ReminderActivity extends AppCompatActivity {

	private TextView reminder, pet, date, time, note;
	private ImageView btnDelete;
	private ProgressDialog pDialog;
	private int id;
	private String rCode = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder);
		
		reminder = (TextView) findViewById(R.id.tvReminderType) ;
		pet = (TextView) findViewById(R.id.tvPet);
		date = (TextView)findViewById(R.id.tvDate);
		time = (TextView) findViewById(R.id.tvTime);
		note = (TextView)findViewById(R.id.tvNote);
		btnDelete = (ImageView)findViewById(R.id.btnDelete);

		String reminderStr = "";
		String petStr = "";
		String dateStr = "";
		String timeStr = "";
		String noteStr = "";
		
		
		Bundle extras = getIntent().getExtras();
		if(extras != null)
		{
			reminderStr = extras.getString("type");
			petStr = extras.getString("pet");
			dateStr = extras.getString("date");
			timeStr = extras.getString("time");
			noteStr = extras.getString("note");
			rCode = extras.getString("rCode");
			id = extras.getInt("id");
			
			reminder.setText(reminderStr);
			pet.setText(petStr);
			date.setText(dateStr);
			time.setText(timeStr);
			note.setText(noteStr);
		}
		
		btnDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String result = "";

				if(isNetworkAvailable())
				{
					try {
						result = new DeleteReminder().execute().get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					debugMsg(Utils.ErrorMessage.internetConnection);
				}
				
			}
		});
	}

	@Override
	public void onPause() {
	    super.onPause();

	    if ((pDialog != null) && pDialog.isShowing())
	    	pDialog.dismiss();
	    pDialog = null;
	}
	
	private class DeleteReminder extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ReminderActivity.this);
			pDialog.setMessage("Deleting reminder..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		};
		
		protected String doInBackground(String... args) 
		{
			String result = "";
			if(id != -1)
			{
				RequestParams params = new RequestParams();
				params.put("id", Integer.toString(id));
				Request request = new Request(PhpFiles.deleteReminder, Utils.RequestActions.POST.toString(), params);
				
				JSONObject json = request.response;
				if(json != null)
				{
					try {
						int success = json.getInt(Utils.REQUEST_RESULT);
						
						if (success == 1) 
						{
							CancelReminder(rCode);
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
			
			return result;
		}
		
		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
			protected void onPostExecute(String result) {
				// dismiss the dialog once done
			super.onPostExecute(result);
			pDialog.dismiss();
		}
	}
	
	private void CancelReminder(String rCode)
	{
		if (CurrentData.alarmManager == null)
        {
        	CurrentData.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);	
        }
		
		Intent it = new Intent(getApplicationContext(), AlarmReceiver.class);	
		int rCodeInt = Integer.parseInt(rCode);
		  
		PendingIntent pi = PendingIntent.getService(getApplicationContext() , rCodeInt , it, PendingIntent.FLAG_UPDATE_CURRENT);
		CurrentData.alarmManager.cancel(pi);
		
		debugMsg("Successfully deleted reminder!");
		
   		Intent i = new Intent(getApplicationContext(), RemindersActivity.class);
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
	    Intent i = new Intent(getApplicationContext(), RemindersActivity.class);
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
