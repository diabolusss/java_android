package org.rusak.contactblocker.call;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


public class CallReceiver extends BroadcastReceiver {
    
	@Override
    public void onReceive(Context context, Intent intent) { 
		Log.d("CallReceiver","onReceive");
				
		//solutioon 2
    	try {
               // TELEPHONY MANAGER class object to register one listner
                TelephonyManager tmgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        
                //Create Listner
                MyPhoneStateListener PhoneListener = new MyPhoneStateListener(context);
                
                //int	LISTEN_CALL_FORWARDING_INDICATOR	Listen for changes to the call-forwarding indicator. 
                //int	LISTEN_CALL_STATE	 				Listen for changes to the device call state. 
                //int	LISTEN_CELL_INFO	 				Listen for changes to observed cell info. 
                //int	LISTEN_CELL_LOCATION	 			Listen for changes to the device's cell location. Note that this will result in frequent callbacks to the listener. 
                //int	LISTEN_DATA_ACTIVITY	 			Listen for changes to the direction of data traffic on the data connection (cellular). 
                //int	LISTEN_DATA_CONNECTION_STATE	 	Listen for changes to the data connection state (cellular). 
                //int	LISTEN_MESSAGE_WAITING_INDICATOR	Listen for changes to the message-waiting indicator. 
                //int	LISTEN_NONE	 						Stop listening for updates. 
                //int	LISTEN_SERVICE_STATE	 			Listen for changes to the network service state (cellular). 
                //int	LISTEN_SIGNAL_STRENGTH	 			This constant was deprecated in API level 7. by LISTEN_SIGNAL_STRENGTHS 
                //int	LISTEN_SIGNAL_STRENGTHS	 			Listen for changes to the network signal strengths (cellular).
                // Register listener for LISTEN_CALL_STATE
                tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        
        } catch (Exception e) {
            Log.e("Phone Receive Error", " " + e);
        }//*/

    }

    private class MyPhoneStateListener extends PhoneStateListener {
    	private Context context;
    	public MyPhoneStateListener(Context context){
    	    this.context = context;
    	}
    	
        public void onCallStateChanged(int state, String incomingNumber) {        
            Log.d("MyPhoneListener","onCallStateChanged");
    		
            //Various states are
            //CALL_STATE_IDLE    – when there is no incoming call
            //CALL_STATE_OFFHOOK – when the line is busy
            //CALL_STATE_RINGING – when call is incoming
            switch (state){
	            case TelephonyManager.CALL_STATE_IDLE: 
	            	Log.d("MyPhoneListener","CALL_STATE_IDLE["+state+"]   incoming no:"+incomingNumber);
	               //Toast.makeText(getApplicationContext(), "CALL_STATE_IDLE", Toast.LENGTH_LONG).show();
	               break;
	               
	            case TelephonyManager.CALL_STATE_OFFHOOK:
	            	Log.d("MyPhoneListener","CALL_STATE_OFFHOOK["+state+"]  incoming no:"+incomingNumber);
		               //Toast.makeText(getApplicationContext(), "CALL_STATE_OFFHOOK", Toast.LENGTH_LONG).show();
	               break;
	               
	            case TelephonyManager.CALL_STATE_RINGING:
	            	Log.d("MyPhoneListener","CALL_STATE_RINGING["+state+"]   incoming no:"+incomingNumber);
	               //Toast.makeText(getApplicationContext(), incomingNumber, Toast.LENGTH_LONG).show();   
	               //Toast.makeText(getApplicationContext(), "CALL_STATE_RINGING", Toast.LENGTH_LONG).show();
	            	if(incomingNumber.contains("22010536") 
	            			|| incomingNumber.contains("28919929") 
	            			|| incomingNumber.contains("26732657")){
	            		
	            		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE); 
	                    //Turn ON the mute
	                    //audioManager.setStreamMute(AudioManager.STREAM_RING, true);      
	            		audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
	            		//audioManager.setRingerMode(audioManager.RINGER_MODE_SILENT);
	                   
	            		disconnectCall();
	            		
	            		//Turn OFF the mute     
	                    //audioManager.setStreamMute(AudioManager.STREAM_RING, false);
	            		//audioManager.setRingerMode(audioManager.RINGER_MODE_NORMAL);
	            		audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 10, 0);
	            	}
	            	break;
	                
	           default:
	        	   break;
            }
        }
    }

    public void disconnectCall(){
    	 try {

    	    String serviceManagerName = "android.os.ServiceManager";
    	    String serviceManagerNativeName = "android.os.ServiceManagerNative";
    	    String telephonyName = "com.android.internal.telephony.ITelephony";
    	    
    	    Class<?> telephonyClass;
    	    Class<?> telephonyStubClass;
    	    Class<?> serviceManagerClass;
    	    Class<?> serviceManagerNativeClass;
    	    
    	    Method telephonyEndCall;
    	    Method telephonySilenceRinger;
    	    Object telephonyObject;
    	    Object serviceManagerObject;
    	    
    	    telephonyClass 				= Class.forName(telephonyName);
    	    telephonyStubClass 			= telephonyClass.getClasses()[0];
    	    serviceManagerClass 		= Class.forName(serviceManagerName);
    	    serviceManagerNativeClass	= Class.forName(serviceManagerNativeName);
    	    
    	    Method getService = // getDefaults[29];
    	    		serviceManagerClass.getMethod("getService", String.class);
    	    Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
    	    
    	    Binder tmpBinder = new Binder();
    	    tmpBinder.attachInterface(null, "fake");
    	    
    	    serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
    	    IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
    	    
    	    Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
    	    
    	    telephonyObject = serviceMethod.invoke(null, retbinder);
    	    telephonyEndCall = telephonyClass.getMethod("endCall");
    	    telephonyEndCall.invoke(telephonyObject);
    	    
    	    telephonySilenceRinger = telephonyClass.getMethod("silenceRinger");
    	    telephonySilenceRinger.invoke(telephonyObject);

    	  } catch (Exception e) {
    	    e.printStackTrace();
    	    Log.d("MyPhoneListener", "FATAL ERROR: could not connect to telephony subsystem");
    	    Log.d("MyPhoneListener", "Exception object: " + e); 
    	 }
    	}//*/
	
}