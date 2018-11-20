package org.rusak.contactblocker.schedule;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;

// here is the onReceive method which will be called when boot completed
public class BootCompletedReceiver extends BroadcastReceiver{
     @Override
     public void onReceive(Context context, Intent intent) {
		 //we double check here for only boot complete event
		if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
		     //here we start the lightweight service             
		     Intent serviceIntent = new Intent(context, StartupServiceManager.class);
		     context.startService(serviceIntent);
		}	
	 }
}