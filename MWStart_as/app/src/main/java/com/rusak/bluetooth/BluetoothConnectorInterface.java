package com.rusak.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.ArrayList;

public interface BluetoothConnectorInterface<T,T1> {
    //initiated socket
    BluetoothSocketInterface getUnderlyingServiceSocket();

    //list of candidates to check in case of failure(deviceSocket is null)
    ArrayList<T> getCandidateServiceNamesOfInterest();
    int getCurrentPossibleCandidateId();

    //method to retrieve list of possible candidates
    ArrayList<T> discoverPossibleCandidates(String name) throws IOException;

    //method to initiate socket connection for possible candidate from list for native connection device
    boolean connect(T1 bluetoothDevice) throws IOException;

}
