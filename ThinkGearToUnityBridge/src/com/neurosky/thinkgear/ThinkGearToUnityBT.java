package com.neurosky.thinkgear;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.rusak.functions.Functions;
import org.rusak.utils.BluetoothUtils;
import org.rusak.utils.DataFlusher;
import org.rusak.utils.FileNameUtils;
import org.rusak.utils.FormatUtils;
import org.rusak.utils.Params;

import android.bluetooth.BluetoothAdapter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.KeyEvent;

import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

//project->export as .jar file
public class ThinkGearToUnityBT extends UnityPlayerActivity {
    private volatile DataFlusher flusher;    
    public int DEBUG_LEVEL = Functions.ERR|Functions.WRN|Functions.INF|Functions.DBG;
        
	public static final String TAG = "ThinkGearToUnityBT";
	
	//as seen in TGDevice.class
	//public static final String[] DEVICE_STATE_NAMES = {}; 
	public static final String STATE_BLUETOOTH_ERROR = "STATE_BLUETOOTH_ERROR";
	  /* This is how to declare HashMap */
    SparseArray<String> DEVICE_STATE_NAMES = new SparseArray<String>();
    
	public boolean 
		sendRawEnable 				= true,
		sendEEGPowerEnable 			= true,
		sendESenseEnable 			= true,
		sendESenseBlinkEnable 		= true,
		sendESenseSleepStageEnable 	= true,
		writeEEGFileLogEnable 		= false
		;

	public String lastConnectState = STATE_BLUETOOTH_ERROR;

	public int pressedKeyCode = -1;

	private BluetoothAdapter bluetoothAdapter;
	private TGDevice tgDevice;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * 07-23 00:19:12.501: E/AndroidRuntime(16429): 
		 * Caused by: java.lang.ArrayIndexOutOfBoundsException: length=0; index=0
		 *07-23 00:19:12.501: E/AndroidRuntime(16429): 	
		 *at com.neurosky.thinkgear.ThinkGearToUnityBT.onCreate(ThinkGearToUnityBT.java:56)
		 */
		//http://javascript.info/tutorial/array	
	    DEVICE_STATE_NAMES.put(TGDevice.STATE_IDLE, 		"STATE_IDLE");
	    DEVICE_STATE_NAMES.put(TGDevice.STATE_CONNECTING, 	"STATE_CONNECTING");
	    DEVICE_STATE_NAMES.put(TGDevice.STATE_CONNECTED, 	"STATE_CONNECTED");
	    DEVICE_STATE_NAMES.put(TGDevice.STATE_DISCONNECTED, "STATE_DISCONNECTED");
	    DEVICE_STATE_NAMES.put(TGDevice.STATE_NOT_FOUND, 	"STATE_NOT_FOUND");
	    DEVICE_STATE_NAMES.put(TGDevice.STATE_NOT_PAIRED, 	"STATE_NOT_PAIRED");
	    DEVICE_STATE_NAMES.put(TGDevice.MSG_LOW_BATTERY, 	"MSG_LOW_BATTERY");
	    
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(BluetoothUtils.isBTAvailable(bluetoothAdapter)){
			tgDevice = new TGDevice(bluetoothAdapter, handler);
			Functions.inf(DEBUG_LEVEL, "New TGDevice created. State: "+DEVICE_STATE_NAMES.get(tgDevice.getState()));
			lastConnectState = DEVICE_STATE_NAMES.get(tgDevice.getState());
			UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveConnectState", lastConnectState);			
			
		}else{
			lastConnectState = STATE_BLUETOOTH_ERROR;
			Functions.err(DEBUG_LEVEL, "Failed to attach bluetooth adapter.");
			UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveConnectState", lastConnectState);
			return;			
		}
		
		if(writeEEGFileLogEnable){
			Functions.inf(DEBUG_LEVEL, "Starting log flusher.");
			startFlusher();
		}
		
		printSettings();
	}
	
	public void printSettings(){
		Functions.inf(DEBUG_LEVEL, "DEBUG_LEVEL:"+DEBUG_LEVEL);
		Functions.inf(DEBUG_LEVEL, "sendRawEnable:"+sendRawEnable);
		Functions.inf(DEBUG_LEVEL, "sendEEGPowerEnable:"+sendEEGPowerEnable);
		Functions.inf(DEBUG_LEVEL, "sendESenseEnable:"+sendESenseEnable);
		Functions.inf(DEBUG_LEVEL, "sendESenseBlinkEnable:"+sendESenseBlinkEnable);
		Functions.inf(DEBUG_LEVEL, "sendESenseSleepStageEnable:"+sendESenseSleepStageEnable);
		Functions.inf(DEBUG_LEVEL, "writeEEGFileLogEnable:"+writeEEGFileLogEnable);
	}

	@Override
	 public void onConfigurationChanged(Configuration newConfig) {
	  Functions.dbg(DEBUG_LEVEL, "onConfigurationChanged called.");
	  super.onConfigurationChanged(newConfig);
	  if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
		  Functions.dbg(DEBUG_LEVEL, "onConfigurationChanged - ORIENTATION_LANDSCAPE");
		  
	  } else {
		  Functions.dbg(DEBUG_LEVEL, "onConfigurationChanged - ORIENTATION_PORTRAIT");
	  }
	 }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		pressedKeyCode = keyCode;
		UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener", "receiveRemoteKeyCode", ""+ keyCode);
		return super.onKeyDown(keyCode, event);
	}

	private final Handler handler = new Handler() {
		private int lastESenseMeditation 	= 0;
		private int lastESenseAttention 	= 0;
		private int lastESenseSleepStage 	= 0;
		private int lastPoorSignal = 200;

		private TGEegPower lastEEGPower = new TGEegPower(0, 0, 0, 0, 0, 0, 0, 0);
		
		private int lastRawData = 0;
		private int lastHeartRate = 0;
		private int lastRawCount = 0;		

        private final List<Integer> BLINK_EMPTY = Arrays.asList(0);
        private List<Integer> lastESenseBlink = new LinkedList<Integer>();
        
        private Params params = new Params();
		private int LAST_PRINT_FLAG = 0;
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TGDevice.MSG_STATE_CHANGE:
				switch (msg.arg1) {
					case TGDevice.STATE_IDLE:
						//break;
					case TGDevice.STATE_CONNECTING:
						//lastConnectState = DEVICE_STATE_NAMES.get(TGDevice.STATE_CONNECTING);
						//UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveConnectState", lastConnectState);
						//break;						
					case TGDevice.STATE_NOT_FOUND:
						//lastConnectState = DEVICE_STATE_NAMES.get(TGDevice.STATE_NOT_FOUND);
						//UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveConnectState", lastConnectState);
						//break;	
					case TGDevice.STATE_DISCONNECTED:
						Functions.dbg(DEBUG_LEVEL, "TGDevice.MSG_STATE_CHANGE Event. State: "+DEVICE_STATE_NAMES.get(msg.arg1));
						
						lastConnectState = DEVICE_STATE_NAMES.get(msg.arg1);
						UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveConnectState", lastConnectState);
						break;
						
					case TGDevice.STATE_CONNECTED:
						Functions.dbg(DEBUG_LEVEL, "TGDevice.MSG_STATE_CHANGE Event. State: "+DEVICE_STATE_NAMES.get(msg.arg1));
					
						lastConnectState = DEVICE_STATE_NAMES.get(TGDevice.STATE_CONNECTED);
						tgDevice.start();
						UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveConnectState", lastConnectState);
						break;
					}
				break;

			case TGDevice.MSG_POOR_SIGNAL:
				Functions.dbg(DEBUG_LEVEL, "TGDevice.MSG_POOR_SIGNAL Event.");
				
				lastPoorSignal = msg.arg1;
				UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receivePoorSignal", ""+lastPoorSignal);
				break;
				
			case TGDevice.MSG_RAW_DATA:
				//Functions.dbg(DEBUG_LEVEL, "TGDevice.MSG_RAW_DATA Event.");
				
				lastRawData = msg.arg1;
				if(sendRawEnable){
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveRawdata", ""+lastRawData);
				}
				if(writeEEGFileLogEnable){
					//Functions.dbg(DEBUG_LEVEL, "doFlushData called.");
					doFlushData();
				}
				break;

			case TGDevice.MSG_ATTENTION:
				Functions.dbg(DEBUG_LEVEL, "TGDevice.MSG_ATTENTION Event.");
				
				lastESenseAttention = (msg.arg1);
				if(sendESenseEnable){
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveAttention", ""+lastESenseAttention);
				}
				break;
				
			case TGDevice.MSG_MEDITATION:
				Functions.dbg(DEBUG_LEVEL, "TGDevice.MSG_MEDITATION Event.");
				
				lastESenseMeditation = (msg.arg1);
				if(sendESenseEnable){
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveMeditation", ""+lastESenseMeditation);
				}
				break;
				
			case TGDevice.MSG_BLINK:
				Functions.dbg(DEBUG_LEVEL, "TGDevice.MSG_BLINK Event.");
				
				int blink = msg.arg1;
				lastESenseBlink.add(blink);
				if(sendESenseBlinkEnable){
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveBlink", ""+blink);
				}
				break;
				
			case TGDevice.MSG_SLEEP_STAGE:
				Functions.dbg(DEBUG_LEVEL, "TGDevice.MSG_SLEEP_STAGE Event.");
				
                lastESenseSleepStage = msg.arg1;
                if(sendESenseSleepStageEnable){
                	UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveSleepStage", ""+lastESenseSleepStage);	
                }
                break;
				
			case TGDevice.MSG_RAW_COUNT:
				lastRawCount = (msg.arg1);
				break;
				
			case TGDevice.MSG_LOW_BATTERY:
				lastConnectState = DEVICE_STATE_NAMES.get(TGDevice.MSG_LOW_BATTERY);
				UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveConnectState", lastConnectState);

				Functions.inf(DEBUG_LEVEL, "Received message - "+lastConnectState);
				break;				
				
			case TGDevice.MSG_EEG_POWER:
				Functions.dbg(DEBUG_LEVEL, "TGDevice.MSG_EEG_POWER Event.");
				
				TGEegPower eeg = (TGEegPower) msg.obj;
				
				if(sendEEGPowerEnable){
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveDelta", 	""+eeg.delta	);
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveTheta", 	""+eeg.theta	);
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveLowGamma", ""+eeg.lowGamma	);
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveLowBeta", 	""+eeg.lowBeta	);
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveLowAlpha", ""+eeg.lowAlpha	);
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveHighGamma",""+eeg.midGamma	);
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveHighBeta", ""+eeg.highBeta	);
					UnityPlayer.UnitySendMessage("ThinkGearToUnityEventListener","receiveHighAlpha",""+eeg.highAlpha);
				}
				
				lastEEGPower = eeg;
				break;

			case TGDevice.MSG_RAW_MULTI:
				break;
				
			case TGDevice.MSG_HEART_RATE:
				lastHeartRate = (msg.arg1);
				break;
				
			default:
				break;
			}
		}
		
		private void doFlushData() {
			//skip faulty data
			if(lastPoorSignal != 0) return;
            params.flush(            		
            		lastEEGPower.delta,
            		lastEEGPower.highAlpha,
            		lastEEGPower.highBeta,
            		lastEEGPower.lowAlpha,
            		lastEEGPower.lowBeta,
            		lastEEGPower.lowGamma,
            		lastEEGPower.midGamma,
            		lastEEGPower.theta,
                    lastESenseAttention,
                    lastESenseMeditation,
                    (lastESenseBlink.size() == 0) ? BLINK_EMPTY : lastESenseBlink,
                    lastRawCount,
                    lastRawData,
                    lastPoorSignal,
                    lastESenseSleepStage
            );

            if (flusher != null) flusher.add(Functions.arrayToString(params.getLogParams(), ";"));
            
            lastESenseBlink.clear();
            lastHeartRate = 0;
            LAST_PRINT_FLAG = 0;
            lastESenseSleepStage = 0;
        }
	};

	@Override
	protected void onDestroy() {		
		super.onDestroy();
		if (!isFinishing()) 
			//just orientation change
			return;//{
		
		Functions.inf(DEBUG_LEVEL, "onDestroy received. Cleaning resources.");
		tgDevice.close();
		stopFlusher();
        //} else {
            //just orientation change
        //}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}		

	public void connectWithRaw() {
		Functions.inf(DEBUG_LEVEL, "connectWithRaw: tgDevice.getState()="+tgDevice.getState()+", TGDevice.STATE_CONNECTING:"+TGDevice.STATE_CONNECTING+", TGDevice.STATE_CONNECTED:"+TGDevice.STATE_CONNECTED);
		if(tgDevice.getState() == TGDevice.STATE_CONNECTING || tgDevice.getState() == TGDevice.STATE_CONNECTED) return;
		
		//if (tgDevice.getState() != TGDevice.STATE_CONNECTING
		//		&& tgDevice.getState() != TGDevice.STATE_CONNECTED) {

		Functions.inf(DEBUG_LEVEL, "Starting TGDevice stream with raw.");
		tgDevice.connect(true);
		
		if(writeEEGFileLogEnable){
			Functions.inf(DEBUG_LEVEL, "Starting log flusher.");
			startFlusher();
		}
		//}
	}


	public void connectNoRaw() {
		Functions.inf(DEBUG_LEVEL, "connectNoRaw: tgDevice.getState()="+tgDevice.getState()+", TGDevice.STATE_CONNECTING:"+TGDevice.STATE_CONNECTING+", TGDevice.STATE_CONNECTED:"+TGDevice.STATE_CONNECTED);
		if(tgDevice.getState() == TGDevice.STATE_CONNECTING || tgDevice.getState() == TGDevice.STATE_CONNECTED) return;
		
		//if (tgDevice.getState() != TGDevice.STATE_CONNECTING
		//		&& tgDevice.getState() != TGDevice.STATE_CONNECTED) {
		Functions.inf(DEBUG_LEVEL, "Starting TGDevice stream.");
		tgDevice.connect(false);
		
		if(writeEEGFileLogEnable){
			Functions.inf(DEBUG_LEVEL, "Starting log flusher.");
			startFlusher();
		}
		//}
	}	
	
	public void start(){
		Functions.inf(DEBUG_LEVEL, "before start() called. tgDevice.getState()="+tgDevice.getState());
		if(tgDevice == null || tgDevice.getState() != TGDevice.STATE_CONNECTED) return;
		tgDevice.start();
		Functions.inf(DEBUG_LEVEL, "after start() called. tgDevice.getState()="+tgDevice.getState());
	}
	
	public void stop(){
		Functions.inf(DEBUG_LEVEL, "before stop() called. tgDevice.getState()="+tgDevice.getState());
		if(tgDevice == null || tgDevice.getState() != TGDevice.STATE_CONNECTED) return;
		tgDevice.stop();
		Functions.inf(DEBUG_LEVEL, "after stop() called. tgDevice.getState()="+tgDevice.getState());
	}

	public void disconnect() {
		tgDevice.close();
		stopFlusher();
	}
	
	private void startFlusher() {
		if (flusher != null) return;
        flusher = new DataFlusher(FileNameUtils.getFileName(tgDevice, "log", sendRawEnable));
        flusher.start();
        flusher.add(Functions.arrayToString(Params.getLogParamNames(), ";"));
    }

    private void stopFlusher() {
        if (flusher == null) return;
        flusher.stop();
        flusher = null;
    }
}