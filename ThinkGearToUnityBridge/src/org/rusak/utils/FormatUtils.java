package org.rusak.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fedor.belov on 06.11.13.
 */
public class FormatUtils {

    private static SimpleDateFormat timeFormat = new SimpleDateFormat("k:mm:ss.SSS");

    public static String millisToLogTime(Long millis) {
        return timeFormat.format(new Date(millis));
    }   

}
