package mobi.omegacentauri.mwstart;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.rusak.bluetooth.BluetoothConnector;
import org.rusak.bluetooth.BluetoothSocketWrapper;
import org.rusak.bluetooth.Headset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MWStart extends Activity {
	private static final String TAG = "MWStart";
	private static final String PREF_LAST_DEVICE = "lastDevice";
	private BluetoothAdapter btAdapter;
	public static TextView message;
	public static TextView log;
	
	private SharedPreferences options;
	private ArrayAdapter<String> deviceSelectionAdapter;
	private boolean brainLinkMode = false;
	private Spinner deviceSpinner;
	private ArrayList<BluetoothDevice> devs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		options = PreferenceManager.getDefaultSharedPreferences(this);

		Log.v("MWStart", "OnCreate");
		
		setContentView(R.layout.main);
		
		message = (TextView)findViewById(R.id.message);
		log = (TextView)findViewById(R.id.txtDebug);
		deviceSpinner = (Spinner)findViewById(R.id.device_spinner);		
	}
		
	@Override
	public void finish() {
		super.finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	@Override
	public void onBackPressed() {
		Log.v(TAG, "onBackPressed");
	   finish();
	 }
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	public void onClickBtnActivate(View v) {
		int pos = deviceSpinner.getSelectedItemPosition();
		if (pos < 0) {
			Toast.makeText(this, "Select a device", Toast.LENGTH_LONG).show();
			return;
		}
			
		//new InitializeTask(this).execute(devs.get(pos));
		BluetoothConnector btConnector = new BluetoothConnector(devs.get(pos), false, btAdapter, null);
		BluetoothSocketWrapper btSocketWrapper = null;
		try {
			btSocketWrapper = btConnector.connect();
			Headset mwHeadset = new Headset(btSocketWrapper, this);
			mwHeadset.Evolve();
			
		} catch (IOException e) {
			Log.v(TAG, e.getLocalizedMessage());
			message.setText(e.getLocalizedMessage());
		}		
			
		
	}
	
	public void onClickBtnTest(View v) {
		int pos = deviceSpinner.getSelectedItemPosition();
		if (pos < 0) {
			Toast.makeText(this, "Select a device", Toast.LENGTH_LONG).show();
		}
		else {
			//new InitializeTask(this).execute(devs.get(pos));
			BluetoothConnector btConnector = new BluetoothConnector(devs.get(pos), false, btAdapter, null);
			
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v("MWStart", "onResume");
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		devs = new ArrayList<BluetoothDevice>();
		devs.addAll(btAdapter.getBondedDevices());
		Collections.sort(devs, new Comparator<BluetoothDevice>(){
			@Override
			public int compare(BluetoothDevice lhs, BluetoothDevice rhs) {
				return String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
			}});
		ArrayList<String> devLabels = new ArrayList<String>();
		for (BluetoothDevice d : devs) 
			devLabels.add(d.getName()+" ("+d.getAddress()+")");
		
		deviceSelectionAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_item, devLabels);
		deviceSelectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		deviceSpinner.setAdapter(deviceSelectionAdapter);
		String lastDev = options.getString(PREF_LAST_DEVICE, "(none)");
		for (int i = 0 ; i < devs.size() ; i++) {
			if (devs.get(i).getAddress().equals(lastDev))
				deviceSpinner.setSelection(i);
		} 
		
		deviceSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				options.edit().putString(PREF_LAST_DEVICE, devs.get(position).getAddress()).commit();				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		if (devs.size()==0) {
			message.setText("Bluetooth turned off or device not paired.");
		}
		else {
			message.setText("");	
		}
	}	
	
}
