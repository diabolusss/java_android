package org.rusak.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.neurosky.thinkgear.TGDevice;

/**
 * Created by fedor.belov on 06.11.13.
 */
public class BluetoothUtils {

    //http://stackoverflow.com/questions/6662216/display-android-bluetooth-device-name
    public static String getTargetBluetoothName(TGDevice tgDevice){
        BluetoothDevice device = tgDevice.getConnectedDevice();
        String name = (device != null) ? tgDevice.getConnectedDevice().getName() : null;
        return (name != null) ? name : "-";
    }
    
    public static boolean isBTAvailable(BluetoothAdapter bt){
		return (bt != null && bt.isEnabled());
	}
    
    public static int getBTState(BluetoothAdapter bt) {
		if (bt == null) return -1;		
		return bt.getState();		
	}
    
    public static int getBTPairedDeviceNum(BluetoothAdapter bt){
    	if (bt == null) return -1;			
    	return bt.getBondedDevices().size();
	}

}
