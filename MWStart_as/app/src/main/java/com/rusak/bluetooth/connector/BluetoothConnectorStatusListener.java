package com.rusak.bluetooth.connector;

public interface BluetoothConnectorStatusListener {
    void onDefaultEvent(BluetoothConnectorState status, String message);
}
