package org.rusak.utils;

import android.os.Build;

import com.neurosky.thinkgear.TGDevice;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.rusak.functions.Functions;

/**
 * Created by fedor.belov on 06.11.13.
 */
public class FileNameUtils {
	private static final String FILE_EXTENSION = ".log";

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");

    public static String getFileName(TGDevice tgDevice, boolean isRaw) {
        return getFileName(tgDevice, null, isRaw);
    }

    public static String getFileName(TGDevice tgDevice, String fileType, boolean isRaw) {
        Date now = new Date();
        String date = dateFormat.format(now);
        String time = timeFormat.format(now);
        String androidId = (Build.MODEL + "-" + Build.VERSION.RELEASE);
        String bluetoothName = BluetoothUtils.getTargetBluetoothName(tgDevice);
        String postfix = (isRaw) ? "-raw" : "";

        return Functions.cleanIllegalChars(Functions.arrayToString(new Object[]{androidId, bluetoothName, fileType, date, time}, "-") + postfix + FILE_EXTENSION);
    }

    public static String getHistoryFileName(TGDevice tgDevice) {
        Date now = new Date();
        String date = dateFormat.format(now);
        String time = timeFormat.format(now);
        String androidId = (Build.MODEL + "-" + Build.VERSION.RELEASE);
        String bluetoothName = BluetoothUtils.getTargetBluetoothName(tgDevice);

        return "History_" + Functions.cleanIllegalChars(Functions.arrayToString(new Object[]{date, time, androidId, bluetoothName}, "-") + FILE_EXTENSION);
    }

}
