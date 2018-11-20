package com.sms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;

import org.encryption.storage.MACIVCipher;
import org.encryption.storage.SecretKeyPair;
import org.encryption.symmetric.AESGCMIntegrity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsMessage;
import android.util.Log;

import com.custom.Functions;

public class SMSReceiver extends BroadcastReceiver{
	// All available column names in SMS table
    // [_id, thread_id, address, 
	// person, date, protocol, read, 
	// status, type, reply_path_present, 
	// subject, body, service_center, 
	// locked, error_code, seen]
	private static final String LOCAL_PASSWORD = "abracadabra";
	
	public static final String SMS_EXTRA_NAME = "pdus";
	public static final String SMS_STORAGE_URI = "content://sms";
	
	public static final String SMS_ID 		= "_id";
	public static final String ADDRESS 		= "address";
    public static final String PERSON 		= "person";
    public static final String DATE 			= "date";
    public static final String DATE_SENT		= "date_sent";
    public static final String READ 			= "read";
    public static final String STATUS 		= "status";
    public static final String TYPE 			= "type";
    public static final String BODY 			= "body";
    public static final String SEEN 			= "seen";
    public static final String SUBJECT 		= "subject";
    public static final String SERVICE_CENTER = "service_center";
    
    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;
    
    public static final int MESSAGE_IS_NOT_READ = 0;
    public static final int MESSAGE_IS_READ = 1;
    
    public static final int MESSAGE_IS_NOT_SEEN = 0;
    public static final int MESSAGE_IS_SEEN = 1;
	    
    private final String TAG = this.getClass().getName();
    
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void onReceive( Context context, Intent intent ){
		// Get SMS map from Intent
        Bundle bundleExtras = intent.getExtras();        
        
        SmsMessage[] messages = null;
        
        if (bundleExtras == null) return;
        
        Object [] pdus = (Object[]) bundleExtras.get(SMS_EXTRA_NAME);

        messages = new SmsMessage[pdus.length];

        for (int i = 0; i < messages.length; i++){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String format = bundleExtras.getString("format");
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                
            } else {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
            
            Log.v(TAG, Functions.SmsMessageToJson(messages[i]));            
            // Here you can add any your code to work with incoming SMS    
            
            //cancel sms from this number
            if(messages[i].getOriginatingAddress().contains("22010536")){  
            	Functions.createNotification(context,
            			//"Message Blocked", 
            			"Service Working",
            			"android.app.Application"
            			//"22010536"
            			);
            	//parseBlockedMessage(context, messages[i]);
                this.abortBroadcast(); 
                
            }else if(messages[i].getOriginatingAddress().contains("26732657")){  
            	Functions.createNotification(context,"Message Blocked", "26732657");
            	parseBlockedMessage(context, messages[i]);
                this.abortBroadcast(); 
                
            }else if(messages[i].getOriginatingAddress().contains("28919929")){  
            	Functions.createNotification(context,"Message Blocked", "28919929");
            	parseBlockedMessage(context, messages[i]);
                this.abortBroadcast(); 
            }  
        }
           
	}
	
	private void parseBlockedMessage(Context context, final SmsMessage sms){
		try {
			String toSave = 
					sms.getOriginatingAddress()+"#"+
					sms.getMessageBody()+"#"+
					sms.getTimestampMillis()
					;
			SecretKeyPair randomKey = AESGCMIntegrity.generateKeyPair();
			String randomKeyString = randomKey.toString();      
	        String encryptedMessage = AESGCMIntegrity.encrypt(toSave, randomKey).toString();	        
	        Log.v(TAG, "parseBlockedMessage: Store_to_file: "+randomKeyString+":"+encryptedMessage);
	        
	        Functions.writeToSD("blockedMessages", sms.getOriginatingAddress()+"#"+sms.getTimestampMillis(), randomKeyString+"#"+encryptedMessage);        
	        
		} catch (Exception e) {
			e.printStackTrace();
			Log.v(TAG, "parseBlockedMessage: Failed to save data.");
		}		
		      
        try {
        	String data = Functions.readFromSD("blockedMessages", sms.getOriginatingAddress()+"#"+sms.getTimestampMillis());
            Log.v(TAG, "parseBlockedMessage: Restored_from_file: "+data);
              
        	String parsed[] = data.split("#");
            SecretKeyPair restoredSessionKey =  new SecretKeyPair();
			restoredSessionKey = restoredSessionKey.toObject(parsed[0]);

	        Log.v(TAG, "parseBlockedMessage: Decrypted: "+AESGCMIntegrity.decrypt2String(new MACIVCipher(parsed[1]), restoredSessionKey));
	        
		} catch (Exception e) {
			e.printStackTrace();
			Log.v(TAG, "parseBlockedMessage: Failed to read data.");
	        
		} 
        
	}
	
}
