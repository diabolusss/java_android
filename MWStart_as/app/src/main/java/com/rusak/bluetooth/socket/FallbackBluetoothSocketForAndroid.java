package com.rusak.bluetooth.socket;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class FallbackBluetoothSocketForAndroid extends BluetoothSocketForAndroid {
    private BluetoothSocket fallbackSocket;

    public FallbackBluetoothSocketForAndroid(BluetoothSocket tmp) throws BluetoothSocketFallbackException {
        super(tmp);
        try
        {
            Class<?> clazz = tmp.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
            Method m = clazz.getMethod("createRfcommSocket", paramTypes);
            Object[] params = new Object[] {Integer.valueOf(1)};
            fallbackSocket = (BluetoothSocket) m.invoke(tmp.getRemoteDevice(), params);
        }
        catch (Exception e)
        {
            throw new BluetoothSocketFallbackException(e);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return fallbackSocket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return fallbackSocket.getOutputStream();
    }


    @Override
    public void connect() throws IOException {
        if(fallbackSocket.isConnected()){
            fallbackSocket.close();
        }
        fallbackSocket.connect();
    }


    @Override
    public void close() throws IOException {
        fallbackSocket.close();
    }

}