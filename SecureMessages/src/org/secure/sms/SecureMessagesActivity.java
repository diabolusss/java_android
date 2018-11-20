package org.secure.sms;

import java.util.ArrayList;

import org.secure.sms.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SecureMessagesActivity extends Activity implements OnClickListener, OnItemClickListener
{
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setTheme( android.R.style.Theme_Light );
        setContentView(R.layout.main);
        
        /**
         * You can also register your intent filter here.
         * And here is example how to do this.
         *
         * IntentFilter filter = new IntentFilter( "android.provider.Telephony.SMS_RECEIVED" );
         * filter.setPriority( IntentFilter.SYSTEM_HIGH_PRIORITY );
         * registerReceiver( new SmsReceiver(), filter );
        **/
        
        this.findViewById( R.id.UpdateList ).setOnClickListener( this );
    }

    ArrayList<String> smsList = new ArrayList<String>();
    
	public void onItemClick( AdapterView<?> parent, View view, int pos, long id ) 
	{
		try 
		{
		    	String[] splitted = smsList.get( pos ).split("\n"); 
			String sender = splitted[0];
			String encryptedData = "";
			for ( int i = 1; i < splitted.length; ++i )
			{
			    encryptedData += splitted[i];
			}
			String data = sender + "\n" + StringCryptor.decrypt( new String(SmsReceiver.PASSWORD), encryptedData );
			Toast.makeText( this, data, Toast.LENGTH_SHORT ).show();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public void onClick( View v ) 
	{
		ContentResolver contentResolver = getContentResolver();
		Cursor cursor = contentResolver.query( Uri.parse( "content://sms/inbox" ), null, null, null, null);

		int indexBody = cursor.getColumnIndex( SmsReceiver.BODY );
		int indexAddr = cursor.getColumnIndex( SmsReceiver.ADDRESS );
		
		if ( indexBody < 0 || !cursor.moveToFirst() ) return;
		
		smsList.clear();
		
		do
		{
			String str = "Sender: " + cursor.getString( indexAddr ) + "\n" + cursor.getString( indexBody );
			smsList.add( str );
		}
		while( cursor.moveToNext() );

		
		ListView smsListView = (ListView) findViewById( R.id.SMSList );
		smsListView.setAdapter( new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, smsList) );
		smsListView.setOnItemClickListener( this );
	}
} SmsReceiver.DATE ));
			smsdate = new java.util.Date( Long.parseLong(smsdate) ).toGMTString();
			
			smssubject = cursor.getString(cursor.getColumnIndex( SmsReceiver.SUBJECT ));
			
			smsbody =  cursor.getString(cursor.getColumnIndex( SmsReceiver.BODY ));
			smsbody = StringCryptor.decrypt( new String(SmsReceiver.PASSWORD), smssubject, smsbody );
			
			smsaddr =  cursor.getString(cursor.getColumnIndex( SmsReceiver.ADDRESS ));
			smsaddr = StringCryptor.decrypt( new String(SmsReceiver.PASSWORD), smssubject, smsaddr );
			
			smsservicecentre =  cursor.getString(cursor.getColumnIndex( SmsReceiver.SERVICE_CENTER ));
			smsservicecentre = StringCryptor.decrypt( new String(SmsReceiver.PASSWORD), smssubject, smsservicecentre );
			
			cursor.close();
			
			String data = 
					"Sender: "	+smsaddr+"\n"+
					"Centre: "	+smsservicecentre+"\n"+
					"Text: "	+smsbody+"\n"+
					"Time: "	+smsdate+"\n"
					;
			Log.v(TAG,data);
			Toast.makeText( this, data, Toast.LENGTH_SHORT ).show();
			
		} 		catch (Exception e) 		{
			e.printStackTrace();
		}
	}

	public void onClick( View v ) 	{
		ContentResolver contentResolver = getContentResolver();
		Cursor cursor = contentResolver.query( Uri.parse( SmsReceiver.SMS_STORAGE_URI+"/inbox" ), null, null, null, null);

		int indexID 		= cursor.getColumnIndex( SmsReceiver.SMS_ID );
		int indexBody 		= cursor.getColumnIndex( SmsReceiver.BODY );
		int indexSubject 	= cursor.getColumnIndex( SmsReceiver.SUBJECT );
		int indexAddr 		= cursor.getColumnIndex( SmsReceiver.ADDRESS );
		if ( indexBody < 0 || !cursor.moveToFirst() ) return;
		
		smsList.clear();
		
		do		{

			Log.v(TAG,"$$$$BEGIN$$$$ STORED MSG DATA $$$$$$$$$$");
			for(int i=0; i < cursor.getColumnCount(); i ++){
				String str = cursor.getString(i);
				Log.v(TAG,"\t["+i+"]["+((str!=null)?(str.length()):(0))+"]"+cursor.getColumnName(i)+"="+str);
				
				if(cursor.getColumnName(i).equalsIgnoreCase(SmsReceiver.DATE_SENT) ){
					Log.v(TAG,"\tHASH:");
					MessageDigest md;
					try {
						md = MessageDigest.getInstance("SHA-256");
						md.update(str.getBytes("UTF-8")); // Change this to "UTF-16" if needed
						for(int ii=0; ii < 18536; ii++){
							md.update(md.digest());
						}
						byte[] digest = md.digest();
						Log.v(TAG,"\tHASH18536:"+Functions.convertByteArrayToHexString(digest));
						
						md = MessageDigest.getInstance("SHA-256");
						md.update(str.getBytes("UTF-8")); // Change this to "UTF-16" if needed
						for(int ii=0; ii < 36000; ii++){
							md.update(md.digest());
						}
						digest = md.digest();
						Log.v(TAG,"\tHASH36k:"+Functions.convertByteArrayToHexString(digest));
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e(TAG,"\tHASH FAILED");
					}
					

					
					
				}
			}
			Log.v(TAG,"$$$$END$$$$ STORED MSG DATA $$$$$$$$$$");
			
			if(cursor.getString( indexSubject) == null){

				Log.v(TAG,"Deleted msg by id "+cursor.getString( indexID ));
				contentResolver.delete(
                        Uri.parse(SmsReceiver.SMS_STORAGE_URI+"/" + cursor.getString( indexID )), null, null);
			}else{
				String str = 
						cursor.getString( indexID ) 
				;
				smsList.add( str );
			}
			
		}		while( cursor.moveToNext() );

		cursor.close();
		ListView smsListView = (ListView) findViewById( R.id.SMSList );
		smsListView.setAdapter( new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, smsList) );
		smsListView.setOnItemClickListener( this );
	}
}