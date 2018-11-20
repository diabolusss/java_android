package org.rusak.custom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.rusak.contactblocker.call.R;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.telephony.SmsMessage;
import android.util.Log;

public class Functions{
	private static String TAG = "FUNCTIONS";
	

	@SuppressLint("NewApi")
	public static void createNotification(Context context, String text, String title) {		
	    // Build notification
	    // Actions are just fake
	    Notification noti = new Notification.Builder(context) 
	        .setContentTitle( (title!=null)?(title):(context.getClass().getName()) )
	        .setContentText(text).setSmallIcon(R.drawable.ic_launcher).build();
	    
	    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    
	    // hide the notification after its selected
	    noti.flags |= Notification.FLAG_AUTO_CANCEL;

	    notificationManager.notify(0, noti);
	}
	
	 /**
     * Method to check whether external media available and writable. This is
     * adapted from
     * http://developer.android.com/guide/topics/data/data-storage.html
     * #filesExternal
     */	
    public boolean externalMediaReadable(){
    	// Can only read the media
    	String state = Environment.getExternalStorageState();
    	if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) return true;
    	else return false;
    }
    
    public boolean externalMediaWritable(){
    	String state = Environment.getExternalStorageState();
    	if (Environment.MEDIA_MOUNTED.equals(state)) return true;
    	else return false;
    }
    
    /**
     * Method to write ascii text characters to file on SD card. Note that you
     * must add a WRITE_EXTERNAL_STORAGE permission to the manifest file or this
     * method will throw a FileNotFound Exception because you won't have write
     * permission.
     */
    public static void writeToSD(String dirname, String filename, String data) {
        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-
        // storage.html#filesExternal
        File root = android.os.Environment.getExternalStorageDirectory();
        
        // See
        // http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        File dir = new File(root.getAbsolutePath() + "/"+dirname);
        dir.mkdirs();
        File file = new File(dir, filename);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            //PrintWriter pw = new PrintWriter(f);
            //pw.println("Hi , How are you");
            //pw.println("Hello");
            //pw.flush();
            //pw.close();
            outputStream.write(data.getBytes());
    		outputStream.close();
    		
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you"
                + " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Method to read in a text file placed in the res/raw directory of the
     * application. The method reads in all lines of the file sequentially.
     */
    public static String readFromSD(String dirname, String filename) {
    	//Find the directory for the SD Card using the API
    	//*Don't* hardcode "/sdcard"
    	File sdcard = Environment.getExternalStorageDirectory();
    	
    	//Get the text file
    	File file = new File(sdcard+"/"+dirname,filename);

    	//Read text from file
    	StringBuilder text = new StringBuilder();

    	try {
    	    BufferedReader br = new BufferedReader(new FileReader(file));
    	    String line;

    	    while ((line = br.readLine()) != null) {
    	        text.append(line);
    	        text.append('\n');
    	    }
    	    br.close();
    	    
    	    return text.toString();
    	    
    	}catch (IOException e) {
    		e.printStackTrace();
            Log.i(TAG, "******* File not found. ");
    	}
    	
    	return null;
    }
    
	public static String SmsMessageToJson(SmsMessage sms){
		String result = 
				"{" +
						"\"getDisplayMessageBody\":"+sms.getDisplayMessageBody()+"\n,"+
						"\"getDisplayMessageBodyLength\":"+((sms.getDisplayMessageBody() != null)?(sms.getDisplayMessageBody().length()):(0))+"\n,"+
						"\"getDisplayOriginatingAddress\":"+sms.getDisplayOriginatingAddress()+"\n,"+
						"\"getEmailBody\":"+sms.getEmailBody()+"\n,"+
						"\"getEmailBodyLength\":"+((sms.getEmailBody() != null)?(sms.getEmailBody().length()):(0))+"\n,"+
						"\"getEmailFrom\":"+sms.getEmailFrom()+"\n,"+
						"\"getIndexOnIcc\":"+sms.getIndexOnIcc()+"\n,"+
						"\"getMessageBody\":"+sms.getMessageBody()+"\n,"+
						"\"getMessageBodyLength\":"+((sms.getMessageBody() != null)?(sms.getMessageBody().length()):(0))+"\n,"+
						"\"getOriginatingAddress\":"+sms.getOriginatingAddress()+"\n,"+
						"\"getOriginatingAddress\":"+sms.getProtocolIdentifier()+"\n,"+
						"\"getPseudoSubject\":"+sms.getPseudoSubject()+"\n,"+
						"\"getServiceCenterAddress\":"+sms.getServiceCenterAddress()+	"\n,"+					
						"\"getStatus\":"+sms.getStatus()+"\n,"+
						"\"getStatus\":"+sms.getStatusOnIcc()+"\n,"+
						"\"getTimestampMillis\":"+sms.getTimestampMillis()+"\n,"+
						"\"isEmail\":"+sms.isEmail()+"\n,"+
						"\"isStatusReportMessage\":"+sms.isStatusReportMessage()+
						
				"}"
			;
		/*
		 * {
		 * 	"getDisplayMessageBody":(error 0) {msg type not recognized 0},
		 * 	"getDisplayOriginatingAddress":colt@toshiba.gotdns.ch,
		 * 	"getEmailBody":(error 0) {msg type not recognized 0},
		 * 	"getEmailFrom":colt@toshiba.gotdns.ch,
		 * 	"getIndexOnIcc":-1,
		 * 	"getMessageBody":colt@toshiba.gotdns.ch (error 0) {msg type not recognized 0},
		 * 	"getOriginatingAddress":100,
		 * 	"getOriginatingAddress":0,
		 * 	"getPseudoSubject":,
		 * 	"getServiceCenterAddress":+37129599994,
		 * 	"getStatus":0,
		 * 	"getStatus":-1,
		 * 	"getTimestampMillis":1447102250000,
		 * 	"isEmail":true,
		 * 	"isStatusReportMessage":false
		 * }
		*/
		return result;
	}
}
