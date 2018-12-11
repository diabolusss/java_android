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

import com.rusak.bluetooth.connector.BluetoothConnectorForAndroid;
import com.rusak.bluetooth.connector.BluetoothConnectorState;
import com.rusak.bluetooth.connector.BluetoothConnectorStatusListener;
import com.rusak.bluetooth.socket.BluetoothSocketInterface;
import com.rusak.lib.enums.process.State;
import com.rusak.lib.enums.process.SubState;
import com.rusak.lib.functions.neurosky.mindwave.TGAMSwitchStatus;
import com.rusak.lib.functions.neurosky.mindwave.TGAMSwitchStatusEventListener;
import com.rusak.lib.functions.neurosky.mindwave.TGAMSwitchToRawMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MWStart extends Activity {
	public static final String TAG = "MWStart";
	private static final String PREF_LAST_DEVICE = "lastDevice";
	private BluetoothAdapter btAdapter;

	public static TextView message;
	public static TextView log;

	//Interface for accessing and modifying preference data returned by Context.getSharedPreferences(String, int)
	private SharedPreferences persistentAppPreferences;
	private ArrayAdapter<String> deviceSelectionAdapter;

	private boolean brainLinkMode = false;

	private Spinner btDeviceListSpinner;
	private ArrayList<BluetoothDevice> btDeviceList;

	private ProgressDialog progressDialog;


	private static final UUID HC05_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	BluetoothConnectorForAndroid btConnector;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("MWStart", "OnCreate");
		setContentView(R.layout.main);

		progressDialog = new ProgressDialog(this);
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		persistentAppPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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


		btConnector = new BluetoothConnectorForAndroid(btDeviceList.get(pos), new ArrayList<UUID>(Arrays.asList(new UUID[]{HC05_UUID})));
		btConnector.setBluetoothConnectorStatusListener(bluetoothConnectorStatusListener);

        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        }
        try{
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorService.execute(btConnector);
			executorService.shutdownNow();

		} catch (Exception e) {
            Log.v(TAG, "Connector failed: "+e.getLocalizedMessage());
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

	/******************************************************
	 * Helpers
	 *****************************************************/
		private void initProcessDialog(){
			progressDialog.show();

			progressDialog.setMessage("Please wait...");
			log.append("Please wait...\n");
		}

		private void changeTGAMMode() throws IOException,NullPointerException {
			if(btConnector == null){
				throw new NullPointerException("Bluetooth connector is not initialized!");
			}

			final BluetoothSocketInterface btSocketWrapper = btConnector.getUnderlyingServiceSocket();

			TGAMSwitchToRawMode headsetEvolver = new TGAMSwitchToRawMode(btSocketWrapper.getInputStream(), btSocketWrapper.getOutputStream());
			headsetEvolver.setListener(tgamModeSwitcherCallback);

			//new Thread(headsetEvolver).run();
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorService.execute(headsetEvolver);
			executorService.shutdownNow();
		}

	/******************************************************
	 * Native activity listeners
	 ******************************************************/
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

		@Override
		public void onResume() {
			super.onResume();
			Log.v("MWStart", "onResume");

			try {
				// (1) Make sure that the device supports Bluetooth and Bluetooth is on
				btAdapter = BluetoothAdapter.getDefaultAdapter();
				if (btAdapter == null || !btAdapter.isEnabled()) {
					Toast.makeText(
							this,
							"Please enable your Bluetooth and re-run this program !",
							Toast.LENGTH_LONG).show();
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.i(TAG, "error:" + e.getMessage());
				return;
			}

			//get list of bonded devices
			btDeviceList = new ArrayList<BluetoothDevice>();
			btDeviceList.addAll(btAdapter.getBondedDevices());
			Collections.sort(btDeviceList, new Comparator<BluetoothDevice>(){
				@Override
				public int compare(BluetoothDevice lhs, BluetoothDevice rhs) {
					return String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
				}}
			);

			if (btDeviceList.size()==0) {
				message.setText("No bluetooth device is paired.");
				return;

			}else {
				message.setText("");
			}

			ArrayList<String> devLabels = new ArrayList<String>();
			for (BluetoothDevice d : btDeviceList) {
				devLabels.add(d.getName() + " (" + d.getAddress() + ")");
			}

			{
				deviceSelectionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, devLabels);
				deviceSelectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				btDeviceListSpinner.setAdapter(deviceSelectionAdapter);
			}

			String lastDev = persistentAppPreferences.getString(PREF_LAST_DEVICE, "(none)");

			for (int i = 0; i < btDeviceList.size() ; i++) {
				if (btDeviceList.get(i).getAddress().equals(lastDev)) {
					btDeviceListSpinner.setSelection(i);
				}
			}

			btDeviceListSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
					persistentAppPreferences.edit().putString(PREF_LAST_DEVICE, btDeviceList.get(position).getAddress()).commit();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {/*do nothing*/}
			});
		}

	/************************************************
	 * Callbacks
	 ***********************************************/
		private BluetoothConnectorStatusListener bluetoothConnectorStatusListener = new BluetoothConnectorStatusListener(){
			/**
			 * TODO collect statuses with messages into buffer and show them independently
			 * @param status
			 * @param msg
			 */
			@Override
			public void onDefaultEvent(final BluetoothConnectorState status, final String msg) {
				Log.v(TAG, "BtConnEvent:{status:"+status.name()+ ", message: "+msg+"}");
				if(status.state.equals(State.INIT)) {
					if (status.subState.equals(SubState.PROCESSING)) {
						MWStart.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								initProcessDialog();
							}
						});
					}

				}else if(status.state.equals(State.PROCESSING)) {
					//if (subStatus.equals(BluetoothConnectorState.SubState.CONNECTING)) {
						MWStart.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								log.append(msg+"\n");
								progressDialog.setMessage(msg);

								message.setText(msg);
							}
						});
					//}
				}else if(status.state.equals(State.FINISHED)) {
					if(status.subState.equals(SubState.CONNECTED)){
						MWStart.this.runOnUiThread(new Runnable() {
						   @Override
						   public void run() {
							   message.setText("TGAM service found.");
						   }
					   });

						try {
							changeTGAMMode();

						} catch (final Exception e) {

							/*MWStart.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									message.setText("Exception caught: "+e.getLocalizedMessage());
								}
							});*/
						}

					}else {
						MWStart.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								progressDialog.cancel();
								message.setText("Connection failed.");
							}
						});
					}
				}

			}
		};

		private TGAMSwitchStatusEventListener tgamModeSwitcherCallback = new TGAMSwitchStatusEventListener(){

			/**
			 * TODO collect statuses with messages into buffer and show them independently
			 * @param status
			 * @param msg
			 */
			@Override
			public void onDefaultEvent(final TGAMSwitchStatus status, final String msg){
				Log.v(TAG, "TGAMEvent {status:"+status.name()+ ", message: "+msg+"}");

				 if(status.state.equals(State.FINISHED)) {
						MWStart.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								progressDialog.cancel();
								message.setText(
										(status.subState.equals(SubState.DONE))
										?"Mode changed successfully."
										:"Failed to change mode."
								);
							}
						});

				}else{
					if(msg != null && msg.isEmpty()){
						return;
					}
					MWStart.this.runOnUiThread(new Runnable() {
						 @Override
						 public void run() {
							log.append(msg+"\n");
							message.setText(msg);
							progressDialog.setMessage(msg);
						 }
					});
				}

			}
		};

}

