package org.rusak.bluetooth;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mobi.omegacentauri.mwstart.MWStart;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.rusak.neurosky.mindwave.PacketUtils;

@Deprecated
public class Headset {
	private boolean status;
	private String message;
	
	private final String TAG = "Headset";	

	private static final byte[] UPSCALED02ALT = new byte[] {0x00, 0x7E, 0x00, 0x00, 0x00, (byte)0xF8};
	private static final byte[] UPSCALED02 = new byte[] {0x00, (byte)0xF8, 0x00, 0x00, 0x00, (byte)0xE0};
		
	private int tryCount = 3;

	private BluetoothSocketWrapper socketWrapper;
	
	//to execute asynctask
	private Context context;
	
	//to post a message
	
	public Headset(BluetoothSocketWrapper socketWrapper, Context c){
		this.socketWrapper = socketWrapper;
		this.context = c;
	}
	
	public void Evolve(){
		new RunTaskEvolve(context).execute(socketWrapper);
	}
	
	//android.os.AsyncTask<Params, Progress, Result>
	class RunTaskEvolve extends AsyncTask<BluetoothSocketWrapper, String, String>{
		private ProgressDialog progressDialog;

		public RunTaskEvolve(Context c) {
			progressDialog = new ProgressDialog(c);
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(BluetoothSocketWrapper... socketWrapper) {
			String msg = "";		
			InputStream streamIn = null;
			OutputStream streamOut = null;
			
			msg="Getting I/O stream...";
			Log.v(TAG, msg);
			publishProgress(msg);
			try {
				streamIn = socketWrapper[0].getInputStream();
				streamOut = socketWrapper[0].getOutputStream();
				
			} catch (IOException e) {
				msg = "Bluetooth socket stream error.";
				Log.v(TAG, msg+" ERR#"+e.getLocalizedMessage());
				publishProgress(msg);
				status = false;
				return msg;
			}
			
			try {								
				msg="Verifying connection...";
				Log.v(TAG, msg);
				publishProgress(msg);
				boolean isEvolved = PacketUtils.isPacketRaw(streamIn);
				if(isEvolved) {
					msg = "Device already in hispeed mode.";
					Log.v(TAG, msg);	
					publishProgress(msg);
					status = true;
					return msg;
				}
									
				int j=0;
				for (j = 0 ; !isEvolved && j < tryCount ; j++ ) {
					msg="Setting up link...";
					Log.v(TAG, msg);
					publishProgress(msg);
					//os.write((j % 2 == 0) ? UPSCALED02ALT : UPSCALED02);
					streamOut.write(UPSCALED02ALT);
					
					sleep(2);
					
					msg="Verifying connection...";
					Log.v(TAG, msg);
					publishProgress(msg);
					
					if(isEvolved = PacketUtils.isPacketRaw(streamIn)) {
						msg = "Successful initiation at "+j+" try." ;
						Log.v(TAG, msg);
						publishProgress(msg);
						status = true;					
						return msg;
					}

					msg="Error verifying, trying again... No."+j;
					Log.v(TAG, msg);
					publishProgress(msg);
				}
				
				msg = "Cannot read valid data.";
				Log.v(TAG, msg);
				publishProgress(msg);
				status = false;				
				return msg;
				
			} catch (Exception e) {
				msg = "doInBackground catched exception.";	
				Log.v(TAG, msg+" ERR#"+e.getLocalizedMessage());
			
			} finally {
				//close opened streams
				try {
					streamIn.close();
					streamOut.close();
					socketWrapper[0].close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.v(TAG, "Exception on stream close. ERR#"+e.getLocalizedMessage()+".");					
				}
			}
			publishProgress(msg);
			
			return msg;
		}	
		
		@Override
		protected void onPostExecute(String message) {
			progressDialog.dismiss();
			MWStart.log.append(
					(status ? "[OK]:" : "[FAILED]:")+message+"\n"
					);
			MWStart.message.setText(
					(status ? "[OK]:" : "[FAILED]:")+message
					);
		}
		
		@Override
		public void onProgressUpdate(String... msg) {
			message = msg[0];
			progressDialog.setMessage(msg[0]);
		}
	}
	
	protected void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Log.v(TAG, "Sleep failed. ERR#"+e.getLocalizedMessage()+".");						
		}
	}

}
