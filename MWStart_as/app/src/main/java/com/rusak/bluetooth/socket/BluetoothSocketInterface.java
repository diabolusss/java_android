package com.rusak.bluetooth.socket;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface BluetoothSocketInterface<T> {
    T getUnderlyingSocket();

    InputStream getInputStream() throws IOException;
    OutputStream getOutputStream() throws IOException;

    void connect() throws IOException;
    void close() throws IOException;

    String getRemoteDeviceAddress();
    String getRemoteDeviceName();
}
