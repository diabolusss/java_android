package com.schedule;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.custom.Functions;

public class TestService extends Service {
	public TestService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	@Override 
	public void onCreate() {
		// TODO Auto-generated method stub
		Functions.createNotification(this.getApplicationContext(), "Service Created", this.getApplicationInfo().className);
		super.onCreate();
	}	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Functions.createNotification(this.getApplicationContext(),"Service Destroy", this.getApplicationInfo().className);
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Functions.createNotification(this.getApplicationContext(),"Service Working", this.getApplicationInfo().className);
		return super.onStartCommand(intent, flags, startId);
	}
	
	
}
