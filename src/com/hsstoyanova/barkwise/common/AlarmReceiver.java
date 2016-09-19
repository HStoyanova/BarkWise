package com.hsstoyanova.barkwise.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.widget.Toast;
import com.hsstoyanova.barkwise.NotificationActivity;;

public class AlarmReceiver extends BroadcastReceiver {
   
     @Override
    public void onReceive(Context context, Intent intent) {
       
       Toast.makeText(context, "Reminder..", Toast.LENGTH_LONG).show();
       Vibrator vib = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);  
       vib.vibrate(2000);
       
       PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
       PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TAG");
       wl.acquire();
       
       Intent i = new Intent(context,NotificationActivity.class); 
         i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         i.putExtra("reminder", intent.getExtras().getString("reminder"));
         i.putExtra("pet", intent.getExtras().getString("pet"));
         i.putExtra("note", intent.getExtras().getString("note"));
         context.startActivity(i);
       }
}