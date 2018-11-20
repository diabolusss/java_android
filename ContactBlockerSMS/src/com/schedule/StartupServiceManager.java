package com.schedule;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.contactblocker.sms.MainActivity;

public class StartupServiceManager extends Service {
	
      @Override
      public IBinder onBind(Intent intent) {
          return null;
      }

      @Override
      /**
       * Now if you want to start (launch) an Android activity at device boot,
       *  then start the activity from the service instead of start it from the receiver.
       *  
       *  Tips:
       *  The onReceive() function in the receiver class expire with in 8 seconds, 
       *  if you will do any time consuming task or waiting more than 8 seconds in onReceive() function, 
       *  then your application will crash with ANR. So better to start a service and
       *   from the service you can do a time consuming task.
       */
      public void onCreate() {
            super.onCreate();
            // do something when the service is created
                        
            // Start Service On Boot Start Up
            Intent service = new Intent(this, TestService.class);
            this.startService(service);
            
            //Start App On Boot Start Up
            Intent App = new Intent(this, MainActivity.class);
            App.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(App);
      }
}