package org.rusak.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.rusak.functions.Functions;

import com.neurosky.thinkgear.ThinkGearToUnityBT;

/**
 * Created by fedor.belov on 20.08.13.
 */
public class DataFlusher {
    private final int DEBUG_LEVEL;
    private static final String TAG = "org.rusak.utils.DataFlusher";

    private volatile boolean active = true;
    private String file;
    private ConcurrentLinkedQueue<Event> events = new ConcurrentLinkedQueue<Event>();

    public DataFlusher(String file) {
        this.file = file;
        this.DEBUG_LEVEL = Functions.ERR|Functions.WRN|Functions.INF|Functions.DBG;
    }
    
    public DataFlusher(String file, int debug_level) {
        this.file = file;
        this.DEBUG_LEVEL = debug_level;
    }

    public void start() {
        try {
            File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + ThinkGearToUnityBT.TAG, file);
            f.getParentFile().mkdirs();
            FileOutputStream fOut = new FileOutputStream(f);
            final PrintWriter osw = new PrintWriter(new OutputStreamWriter(fOut));

            Functions.inf(DEBUG_LEVEL, "Saving data to file"+ f.getAbsolutePath());

            new Thread() {
                @Override
                public void run() {
                    try {
                        while (active) {
                            Event e = events.poll();
                            while (e != null) {
                                if (e.millis > 0) {
                                    osw.println(FormatUtils.millisToLogTime(e.millis) + ";" + e.data);
                                } else {
                                    osw.println(e.data);
                                }

                                osw.flush();

                                e = events.poll();
                            }

                            try {
                                sleep(1000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    } catch (Exception ioe) {
                        Functions.err(DEBUG_LEVEL, "Exception on saving thingear file!"+ ioe.getLocalizedMessage());
                        ioe.printStackTrace();
                    } finally {
                        try {
                            osw.close();
                        } catch (Exception e) {
                            Functions.err(DEBUG_LEVEL, "Exception on closing thingear file!"+ e.getLocalizedMessage());                            
                            e.printStackTrace();
                        }
                    }
                }
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(String data) {
        events.offer(new Event(System.currentTimeMillis(), data));
    }

    public void addWithoutTime(String data) {
        events.offer(new Event(-1, data));
    }

    public void stop() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    private static class Event {
        Long millis;
        String data;

        private Event(long millis, String data) {
            this.millis = millis;
            this.data = data;
        }
    }
}
