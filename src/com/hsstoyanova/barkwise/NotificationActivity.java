package com.hsstoyanova.barkwise;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class NotificationActivity extends AppCompatActivity{
    MediaPlayer m;
    TextView type, pet, note;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        m=MediaPlayer.create(getApplicationContext(), R.raw.navy_alarm);
        m.start();
    	String reminder = "";
    	String pet = "";
    	String note = "";
        
        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
        	reminder = extras.getString("reminder");	
        	pet = extras.getString("pet");	
        	note = extras.getString("note");	
        }

        String message = constructReminderMessage(reminder, pet, note);
        
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(NotificationActivity.this);                    
         dlgAlert.setTitle("Remainder !");
         dlgAlert.setMessage(message);
         dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int whichButton) {

             m.stop();
             dialog.cancel();
             //Intent i = new Intent(getApplicationContext(), RemindersActivity.class);
             //startActivity(i);
         	}
         });
         dlgAlert.show();
     }
  
    
    private String constructReminderMessage(String reminder, String pet, String note)
    {
    	String result = "Remider!";
    	
    	if(reminder != null && pet != null && note != null &&
    			reminder != "" && pet != "" && note != "")
    	{
    		result = "Reminder type:" + reminder + "\n" + "Pet:" + pet + "\n"  + "Note:" + note + ".";
    	}
    	
    	return result;
    }
   
}