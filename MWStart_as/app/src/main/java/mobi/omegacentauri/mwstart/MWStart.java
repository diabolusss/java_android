package mobi.omegacentauri.mwstart;


import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rusak.bluetooth.BluetoothConnectorForAndroid;
import com.rusak.bluetooth.BluetoothConnectorStatusListener;
import com.rusak.bluetooth.BluetoothSocketInterface;
import com.rusak.lib.functions.neurosky.mindwave.TGAMSwitchStatusEventListener;
import com.rusak.lib.functions.neurosky.mindwave.TGAMSwitchToRawMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MWStart extends Activity implements TGAMSwitchStatusEventListener, BluetoothConnectorStatusListener {
	public static final String TAG = "MWStart";
	private static final String PREF_LAST_DEVICE = "lastDevice";
	private BluetoothAdapter btAdapter;
	public static TextView message;
	public static TextView log;
	
	private SharedPreferences options;
	private ArrayAdapter<String> deviceSelectionAdapter;

	private boolean brainLinkMode = false;

	private Spinner btDeviceListSpinner;
	private ArrayList<BluetoothDevice> btDeviceList;

	private ProgressDialog progressDialog;


	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("MWStart", "OnCreate");
		setContentView(R.layout.main);

		progressDialog = new ProgressDialog(this);

		options = PreferenceManager.getDefaultSharedPreferences(this);

		message = (TextView)findViewById(R.id.message);
		log = (TextView)findViewById(R.id.txtDebug);
			log.setMovementMethod(new ScrollingMovementMethod());
		btDeviceListSpinner = (Spinner)findViewById(R.id.device_spinner);
	}

	
	public void onClickBtnActivate(View v) {
		int pos = btDeviceListSpinner.getSelectedItemPosition();
		if (pos < 0) {
			Toast.makeText(this, "Select a device", Toast.LENGTH_LONG).show();
			return;
		}

		boolean isServiceFound = false;
		BluetoothConnectorForAndroid btConnector = new BluetoothConnectorForAndroid(btDeviceList.get(pos), new ArrayList<UUID>(Arrays.asList(new UUID[]{MY_UUID})));
		btConnector.setBluetoothConnectorStatusListener(this);
		//BluetoothConnectorOld btConnector = new BluetoothConnectorOld(btDeviceList.get(pos), false, btAdapter, null);
		try{
			//do in separate thread: publish status done
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			Future future = executorService.submit(btConnector);
			isServiceFound = (boolean) future.get();
			executorService.shutdownNow();

			System.out.println("isServiceFound = " + isServiceFound);

		} catch (Exception e) {
            Log.v(TAG, "Connector failed: "+e.getLocalizedMessage());
		}

		if(!isServiceFound){
            return;
        }
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();

		progressDialog.setMessage("start");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			BluetoothSocketInterface btSocketWrapper = btConnector.getUnderlyingServiceSocket();

			TGAMSwitchToRawMode headsetEvolver = new TGAMSwitchToRawMode(btSocketWrapper.getInputStream(), btSocketWrapper.getOutputStream());
			headsetEvolver.setListener(this);
			new Thread(headsetEvolver).start();


		}  catch (Exception e) {
			Log.v(TAG, "Evolve Caught Ex:"+e.getLocalizedMessage());
			message.setText(e.getLocalizedMessage());
			progressDialog.dismiss();
		}

	}
	
	public void onClickBtnTest(View v) {
		//int pos = btDeviceListSpinner.getSelectedItemPosition();
		//if (pos < 0) {
		//	Toast.makeText(this, "Select a device", Toast.LENGTH_LONG).show();
		//	return;
		//}

		//new InitializeTask(this).execute(btDeviceList.get(pos));
		Toast.makeText(this, "Not implemented yet!", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v("MWStart", "onResume");
		btAdapter = BluetoothAdapter.getDefaultAdapter();

		//device doesnt have bluetooth
		if(btAdapter == null){
			return;
		}

		btDeviceList = new ArrayList<BluetoothDevice>();
		btDeviceList.addAll(btAdapter.getBondedDevices());
		Collections.sort(btDeviceList, new Comparator<BluetoothDevice>(){
			@Override
			public int compare(BluetoothDevice lhs, BluetoothDevice rhs) {
				return String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
			}});
		ArrayList<String> devLabels = new ArrayList<String>();
		for (BluetoothDevice d : btDeviceList)
			devLabels.add(d.getName()+" ("+d.getAddress()+")");
		
		deviceSelectionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, devLabels);
		deviceSelectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		btDeviceListSpinner.setAdapter(deviceSelectionAdapter);
		String lastDev = options.getString(PREF_LAST_DEVICE, "(none)");
		for (int i = 0; i < btDeviceList.size() ; i++) {
			if (btDeviceList.get(i).getAddress().equals(lastDev))
				btDeviceListSpinner.setSelection(i);
		} 
		
		btDeviceListSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				options.edit().putString(PREF_LAST_DEVICE, btDeviceList.get(position).getAddress()).commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		if (btDeviceList.size()==0) {
			message.setText("Bluetooth turned off or device not paired.");
		}
		else {
			message.setText("");	
		}
	}

	@Override
	public void onBluetoothConnectorStartedEvent() {
		Log.v(TAG, "onBluetoothConnectorStartedEvent");
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressDialog.setCancelable(false);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.show();

				progressDialog.setMessage("start");
			}
		});
	}

	/**
	 * TODO collect statuses with messages into buffer and show them independently
	 * @param status
	 * @param msg
	 */
	@Override
	public void onDefaultEvent(final int status, final String msg) {
		Log.v(TAG, "received status "+status+ " with message "+msg);
		this.runOnUiThread(new Runnable() {
		   @Override
		   public void run() {
			   log.append(msg+"\n");
			   progressDialog.setMessage(msg);

			   if(status == 10){
				   progressDialog.dismiss();
			   }else{
				   message.setText(msg);
			   }
		    }
		});

	}

    @Override
    public void onBluetoothConnectorFinishedEvent() {
		Log.v(TAG, "onBluetoothConnectorFinishedEvent");
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				log.append("onBluetoothConnectorFinishedEvent\n");
				progressDialog.dismiss();
			}
		});
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
}

