package org.rusak.neurosky.mindwave;

import android.util.Log;

import org.rusak.bluetooth.BluetoothSocketWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mobi.omegacentauri.mwstart.MWStart;

public class TGAMSwitchToRawMode implements Runnable, TGAMSwitchStatusEventListener {
    private BluetoothSocketWrapper socketWrapper;
    private int maxRetry = 3;

    private TGAMSwitchStatusEventListener listener;

    public TGAMSwitchToRawMode(final BluetoothSocketWrapper socket){
        socketWrapper = socket;
    }

    public TGAMSwitchToRawMode(final BluetoothSocketWrapper socket, final int retries){
        socketWrapper = socket;
        maxRetry = retries;
    }

    @Override
    public void run() {
        InputStream streamIn = null;
        OutputStream streamOut = null;

        //msg="Getting I/O stream...";
        publishStatus(0,0);

        try {
            streamIn = socketWrapper.getInputStream();
            streamOut = socketWrapper.getOutputStream();

        } catch (IOException e) {
            //msg = "Bluetooth socket stream error.";
            publishStatus(9,0);
            return;

        } catch (Exception e) {
            //msg = "doInBackground catched exception.";
            publishStatus(8, 0);
            return;
        }

        try {
            //msg="Verifying connection...";
            publishStatus(1,0);

            if(PacketUtils.isPacketRaw(streamIn)) {
                //msg = "Device already in hispeed mode.";
                publishStatus(5,0);
                return;
            }

            for (int j = 0; j < maxRetry ; j++) {
                //msg="Setting up link...";
                publishStatus(2,j+1);

                //os.write((j % 2 == 0) ? UPSCALED02ALT : UPSCALED02);
                streamOut.write(PacketUtils.UPSCALED02ALT);

                Thread.sleep(20);

                //msg="Verifying connection...";
                publishStatus(1, j+1);

                if(PacketUtils.isPacketRaw(streamIn)) {
                    //msg = "Successful initiation at "+j+" try." ;
                    publishStatus(3, j+1);
                    return;
                }

                //message="Error verifying, trying again... No."+atTry;
                publishStatus(6, j+1);
            }

            //msg = "Cannot read valid data.";
            publishStatus(7, 0);
            return;

        } catch (Exception e) {
            //msg = "doInBackground catched exception.";
            publishStatus(8, 0);

        } finally {
            //close opened streams
            try {

                System.out.print("TGAMSwitchToRawMode streams closed");
                Log.i(MWStart.TAG,"TGAMSwitchToRawMode streams closed");
                streamIn.close();
                streamOut.close();
                socketWrapper.close();

            } catch (IOException e) {
                //nothing to do here
            }
        }
        return;
    }

    @Override
    public void onDefaultEvent(final int status, final String message) {
        System.out.print("TGAMSwitchToRawMode listener");
    }

    /**
     * TODO# change status to enum
     *
     * @param status
     * @param atTry
     */
    private void publishStatus(final int status, final int atTry){
        if(listener == null){
            return; //do nothing
        }

        String message = "";
        if(status == 0){
            message="Getting I/O stream...";

        }else if(status == 1){
            message="Verifying connection...";

        }else if(status == 2){
            message="Setting up link...";

        }else if(status == 3){
            message="Successful initiation at "+atTry+" try." ;

        }else if(status == 5){
            message="Device already in hispeed mode.";

        }else if(status == 6){
            message="Error verifying, trying again... No."+atTry;

        }else if(status == 7){
            message="Cannot read valid data.";

        }else if(status == 8){
            message="Fatal exception caught, thread dead.";

        }else if(status == 9){
            message="Bluetooth socket stream error.";
        }

        listener.onDefaultEvent (status, message);
    }

    /*****************************************************
     * GETters/SETters
     *****************************************************/
        public void setListener(TGAMSwitchStatusEventListener listener) {
            this.listener = listener;
        }

}
