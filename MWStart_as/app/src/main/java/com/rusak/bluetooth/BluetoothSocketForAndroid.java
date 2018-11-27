package com.rusak.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class BluetoothSocketForAndroid implements BluetoothSocketInterface<BluetoothSocket> {
    protected BluetoothSocket socket;

    BluetoothSocketForAndroid(BluetoothSocket tmp) {
        this.socket = tmp;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public String getRemoteDeviceName() {
        return socket.getRemoteDevice().getName();
    }

    @Override
    public void connect() throws IOException {
        if(socket.isConnected()){
            socket.close();
        }

        socket.connect();
    }

    @Override
    public String getRemoteDeviceAddress() {
        return socket.getRemoteDevice().getAddress();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public BluetoothSocket getUnderlyingSocket() {
        return socket;
    }
}
