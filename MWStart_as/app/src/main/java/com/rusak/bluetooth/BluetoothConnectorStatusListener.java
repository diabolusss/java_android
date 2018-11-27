package com.rusak.bluetooth;

public interface BluetoothConnectorStatusListener {
    void onBluetoothConnectorStartedEvent();
    void onDefaultEvent(int status, String message);
    void onBluetoothConnectorFinishedEvent();
}
