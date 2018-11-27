package com.rusak.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;

public class BluetoothConnectorForAndroid implements Callable<Boolean>, BluetoothConnectorInterface<UUID,BluetoothDevice>, BluetoothConnectorStatusListener {

    BluetoothConnectorStatusListener listener;

    //holds actual connection
    private BluetoothSocketInterface<BluetoothSocket> bluetoothServiceSocket;
    private int currentServiceCandidateId = 0;

    private ArrayList<UUID> candidateServiceNamesOfInterest;
    private Boolean forceSecureConnection;

    //actual device we'll try to connect to service
    private BluetoothDevice bluetoothDevice;

    public BluetoothConnectorForAndroid(BluetoothDevice bluetoothDevice, ArrayList<UUID> candidateServiceNamesOfInterest, boolean forceSecureConnection){
        this.candidateServiceNamesOfInterest = candidateServiceNamesOfInterest;
        this.forceSecureConnection = forceSecureConnection;
        this.bluetoothDevice = bluetoothDevice;
    }

    public BluetoothConnectorForAndroid(BluetoothDevice bluetoothDevice, ArrayList<UUID> candidateServiceNamesOfInterest){
        this(bluetoothDevice, candidateServiceNamesOfInterest, false);
    }

    @Override
    public BluetoothSocketInterface getUnderlyingServiceSocket() {
        return bluetoothServiceSocket;
    }

    @Override
    public ArrayList<UUID> getCandidateServiceNamesOfInterest() {
        return candidateServiceNamesOfInterest;
    }

    @Override
    public int getCurrentPossibleCandidateId() {
        return currentServiceCandidateId;
    }

    /**
     * Use BT Device to discover available nearby devices.
     * If name is given, that match only that.
     * @param name
     * @return
     * @throws IOException
     */
    @Override
    public ArrayList<UUID> discoverPossibleCandidates(String name) throws IOException {
        return null;//throw not implemented yet
    }

    @Override
    public boolean connect(BluetoothDevice device) throws IOException {
        boolean success = false;
        String message = "";

        BluetoothSocketInterface<BluetoothSocket> bluetoothServiceSocket;
        while ( (bluetoothServiceSocket = selectCurrentServiceCandidateSocket(device)) != null) {
            //Do i need to make this call here or outside of loop is enough?
            //if(adapter.isDiscovering()) {
            //    adapter.cancelDiscovery();
            //}

            try {
                publishBluetoothConnectorStatusUpdateEvent(1,"Making connection to Android native bt service socket...");
                //https://stackoverflow.com/questions/15889908/getbluetoothservice-called-with-no-bluetoothmanagercallback
                //getBluetoothService() called with no BluetoothManagerCallback - seems to be a bug
                bluetoothServiceSocket.connect();
                success = true;
                break;

            } catch (IOException e) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                //publish event: NativeSocket failed, trying fallback
                publishBluetoothConnectorStatusUpdateEvent(1,"Native service socket failed. Trying fallback Android bt service socket...");

                try {
                    bluetoothServiceSocket = new FallbackBluetoothSocketForAndroid(bluetoothServiceSocket.getUnderlyingSocket());
                    Thread.sleep(30);
                    bluetoothServiceSocket.connect();
                    success = true;
                    break;

                } catch (Exception e1){
                    if(e1 instanceof BluetoothSocketFallbackException){
                        Log.w("BT", "Could not initialize FallbackBluetoothSocketForAndroid classes."+ e.getLocalizedMessage());
                    } else if(e1 instanceof InterruptedException ) {
                        Log.w("BT", "Process interrupted. "+e1.getMessage());
                    } else if (e1 instanceof IOException ) {
                        Log.w("BT", "Fallback failed. Cancelling." + e1.getLocalizedMessage());
                    }

                    publishBluetoothConnectorStatusUpdateEvent(1,"Fallback service socket failed. Ex:"+ e1.getLocalizedMessage());

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }

        if(success){
            this.bluetoothServiceSocket = bluetoothServiceSocket;
        }
        publishFinishedEvent();

        return success;
    }

    /**
     * Initiate socket for current possible candidate in list
     *
     * @param device
     * @return
     * @throws IOException
     */
    private BluetoothSocketInterface selectCurrentServiceCandidateSocket(BluetoothDevice device) throws IOException {
        if(currentServiceCandidateId >= candidateServiceNamesOfInterest.size()){
            return null;//or continue from beginning?
        }

        UUID currentServiceCandidate = candidateServiceNamesOfInterest.get(currentServiceCandidateId++);

        //publish event: Attempting to connect to X via forceSecure
        BluetoothSocket bluetoothServiceSocket;
        if(forceSecureConnection){
            bluetoothServiceSocket = device.createRfcommSocketToServiceRecord(currentServiceCandidate);

        }else{
            bluetoothServiceSocket = device.createInsecureRfcommSocketToServiceRecord(currentServiceCandidate);
        }

        return new BluetoothSocketForAndroid(bluetoothServiceSocket);
    }

    @Override
    public Boolean call() throws Exception {
        publishStartedEvent();

        if(bluetoothDevice == null){
            return false;
        }

        return connect(bluetoothDevice);
    }

    /*******************************************************
     * Dummy events
     *******************************************************/
    @Override
    public void onBluetoothConnectorStartedEvent() {
    }

    @Override
    public void onDefaultEvent(int var1, String var2) {
    }

    @Override
    public void onBluetoothConnectorFinishedEvent() {
    }

    /**********************************************************
     * Event publishers
     ********************************************************/
    private void publishFinishedEvent(){
        if(listener != null) {
            listener.onBluetoothConnectorFinishedEvent();
        }
    }

    private void publishBluetoothConnectorStatusUpdateEvent(int status, String message){
        if(listener != null) {
            listener.onDefaultEvent(status, message);
        }
    }

    private void publishStartedEvent(){
        if(listener != null) {
            listener.onBluetoothConnectorStartedEvent();
        }
    }

    /********************************************************
     * GETters/SETters
     *******************************************************/
    public void setBluetoothConnectorStatusListener(BluetoothConnectorStatusListener listener) {
        this.listener = listener;
    }
}
