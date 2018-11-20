package org.rusak.bluetooth;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mobi.omegacentauri.mwstart.MWStart;
import mobi.omegacentauri.mwstart.R;

import org.rusak.bluetooth.BluetoothConnector.BluetoothSocketWrapper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class Headset {
	public boolean status;	
	public String message;	
	
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
			
			msg="Getting I\\O stream...";
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
				boolean isEvolved = testInputStream(streamIn);
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
					
					if(isEvolved = testInputStream(streamIn)) {
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
	
	/**
	 * Read RAW packet length bytes and check if this packet is correct
	 * @param streamIn
	 * @return
	 */
	public boolean testInputStream(InputStream streamIn){
		clearBuffer(streamIn);
		byte[] data512 = new byte[512]; 
		try {
			readWithTimeout(streamIn, data512, 2000);
		} catch(IOException e) {
			return false;
		}
		
		return testPacketBytes(data512);
	}
	
	/**
	 * Check if received bytes are part of proper RAW mode packet 
	 * 
	 * The Header of a Packet consists of 3 bytes: 
	 * 	two synchronization [SYNC] bytes 
	 * 	 - 0xAA 0xAA, 
	 * 	followed by a [PLENGTH] (Payload length) byte:
	 *   - 0x04 - at high speed(57600)
	 *   - or 0x20 - at low speed(9600)
	 *  and data:
	 *   - 0x80 0x02 - RAW packet, length 0x02(56700)
	 *   - or 0x02 0xXX - signal, value XX(9600);
	 */
	private boolean testPacketBytes(byte[] data) {
//		for (int i = 0 ; i < data.length ; i += 16) {
//			String out = "";
//			for (int j = 0 ; i + j < data.length ; j++)
//				out += String.format("%02x ", data[i+j]);
//			Log.v("MWStart", out);
//		}
		for (int i = 0 ; i < data.length - 8 ; i++) {
			int len;
			if (data[i] == (byte)0xAA && data[i+1] == (byte)0xAA && (len = 0xFF&(int)data[i+2]) == 4 && 
					data[i+3] == (byte)0x80
					) {
				Log.v(TAG, "[OK] RAW packet found [AA AA 04 80 ...] at "+i);
				
				if (i + len + 3 >= data.length) 
					continue;
				byte sum = 0;
				for (int j = i + 3; j < i + 3 + len ; j++) {
					sum += data[j];
				}
				Log.v(TAG, "sum "+sum+" vs "+data[i+3+len]);
				
				return (0xFF&(sum ^ 0xFF)) == (0xFF&data[i + 3 + len]);
			}
		}
		return false;
	}
	
	/**
	 * Read bytes for timeout time.
	 * @param streamIn
	 * @param data
	 * @param timeout
	 * @throws IOException
	 */
	protected void readWithTimeout(InputStream streamIn, byte[] data, int timeout) throws IOException {
		int pos = 0;
		long t1 = System.currentTimeMillis() + timeout;
		
		while (pos < data.length && System.currentTimeMillis() <= t1) {
			int n = streamIn.available();			
			if(n < 0) continue; 
			
			if (pos + n > data.length)
				n = data.length - pos;
			Log.v(TAG, "reading "+n+" bytes");

			streamIn.read(data, pos, n);
			pos += n;
		}
		
		if (pos < data.length)
			new IOException("Timeout occured while reading "+data.length+" bytes at position "+pos+".");
	}
	
	protected void clearBuffer(InputStream is) {
		int avail;
		try {
			avail = is.available();
			is.skip(avail);		
		} catch (IOException e) {
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
