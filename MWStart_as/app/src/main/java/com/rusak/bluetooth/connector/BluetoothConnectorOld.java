package com.rusak.bluetooth.connector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.rusak.bluetooth.socket.BluetoothSocketFallbackException;
import com.rusak.bluetooth.socket.BluetoothSocketForAndroid;
import com.rusak.bluetooth.socket.BluetoothSocketInterface;
import com.rusak.bluetooth.socket.FallbackBluetoothSocketForAndroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Deprecated
public class BluetoothConnectorOld {
    private BluetoothSocketInterface<BluetoothSocket> bluetoothSocket;
    private BluetoothDevice device;
    private boolean secure;
    private BluetoothAdapter adapter;
    private List<UUID> uuidCandidates;
    private int candidate;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
    /**
     * @param device the device
     * @param secure if connection should be done via a secure socket
     * @param adapter the Android BT adapter
     * @param uuidCandidates a list of UUIDs. if null or empty, the Serial PP id is used
     */
    public BluetoothConnectorOld(BluetoothDevice device, boolean secure, BluetoothAdapter adapter, List<UUID> uuidCandidates) {
        this.device = device;
        this.secure = secure;
        this.adapter = adapter;
        this.uuidCandidates = uuidCandidates;

        if (this.uuidCandidates == null || this.uuidCandidates.isEmpty()) {
            this.uuidCandidates = new ArrayList<UUID>();
            this.uuidCandidates.add(MY_UUID);
        }
    }

    public BluetoothSocketInterface connect() throws IOException {
        boolean success = false;
        while (selectSocket()) {
            if(adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }

            try {
                bluetoothSocket.connect();
                success = true;
                break;
            } catch (IOException e) {
                //try the fallback
                try {
                    bluetoothSocket = new FallbackBluetoothSocketForAndroid(bluetoothSocket.getUnderlyingSocket());
                    Thread.sleep(500);
                    bluetoothSocket.connect();
                    success = true;
                    break;
                } catch (BluetoothSocketFallbackException e1) {
                    Log.w("BT", "Could not initialize FallbackBluetoothSocketForAndroid classes.", e);
                } catch (InterruptedException e1) {
                    Log.w("BT", e1.getMessage(), e1);
                } catch (IOException e1) {
                    Log.w("BT", "Fallback failed. Cancelling.", e1);
                }
            }
        }

        if (!success) {
            throw new IOException("Could not connect to device: "+ device.getAddress());
        }

        return bluetoothSocket;
    }
    
    /*public InputStream getInputStream() throws IOException{
    	return bluetoothSocket.getInputStream();
    }
    public OutputStream getOutputStream() throws IOException{
    	return bluetoothSocket.getOutputStream();
    }*/
    
    private boolean selectSocket() throws IOException {
        if (candidate >= uuidCandidates.size()) {
            return false;
        }

        BluetoothSocket tmp;
        UUID uuid = uuidCandidates.get(candidate++);

        Log.i("BT", "Attempting to connect to Protocol: "+ uuid);
        if (secure) {
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } else {
            tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
        }
        bluetoothSocket = new BluetoothSocketForAndroid(tmp);

        return true;
    }

}