package com.rusak.bluetooth.socket;

import com.rusak.lib.enums.process.State;
import com.rusak.lib.enums.process.SubState;

public enum BluetoothSocketState {
    INIT_FAILED         (State.INIT, SubState.FAILED),
    INIT_PROCESSING     (State.INIT, SubState.PROCESSING),

    PROCESSING_CONNECTING   (State.PROCESSING, SubState.CONNECTING),
    PROCESSING_ERROR        (State.PROCESSING, SubState.ERROR),

    FINISHED_CONNECTED  (State.FINISHED, SubState.CONNECTED),
    FINISHED_FAILED     (State.FINISHED, SubState.FAILED),
    ;

    public State state;
    public SubState subState;
    BluetoothSocketState(final State state, final SubState subState){
        this.state = state;
        this.subState = subState;
    }
}
