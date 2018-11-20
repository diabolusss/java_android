package org.rusak.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author colt
 */
public class Functions {
    public static final int DBG1    = 16;
    public static final int DBG    = 8;
    public static final int INF     = 4;
    public static final int WRN    = 2;
    public static final int ERR     = 1;
    
    private static final int LOG_LEVEL = ERR|WRN|INF|DBG|DBG1;          
    
    /**
     * 
     * @param key
     * @param data
     * @return
     */
    public static String getHmacSHA256Hex(String key, String data){
        Mac sha256_HMAC = null;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");        
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return bytesToHex(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
            
        } catch (Exception ex) {
            err(LOG_LEVEL, "Functions[getHmacSHA256Hex]: Failed to generate HMAC SHA256. E:"+ex.getLocalizedMessage());
        }
        
        return "";
    }
    
    /**
     * 
     * @param bytes
     * @return 
     */     
    public static String bytesToHex(byte[] bytes) {
        /*final protected static*/ char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static Long fileSize(String filename){
        try{
            File f = new File(filename);
            return f.length();
            
        }catch(Exception ex){
            wrn(LOG_LEVEL,"Functions[fileSize]: Failed to access file["+filename+"]. E:"+ex.getLocalizedMessage());
        }
        
        return 0L;
    }
    
    /*
     * Fetches all available server properties 
     * @param path
     */
    public static Properties getProperties(String path){
        Properties newProperties = new Properties();
        try {
            InputStream input = new FileInputStream(path);
            newProperties.load(input);
        }
        catch (IOException ex){
            err(LOG_LEVEL,"Functions[getProperties]: Cannot open\\load server properties file. E: "+ex.getLocalizedMessage());
            return null;
        }
        return newProperties;                
    }
    
    public static Properties getProperties(InputStream input){
        Properties newProperties = new Properties();
        try {            
            newProperties.load(input);
        }
        catch (IOException ex){
            err(LOG_LEVEL,"Functions[getProperties]: Cannot load server properties file. E: "+ex.getLocalizedMessage());
            return null;
        }
        return newProperties;                
    }
    
    /**
    * Outputs message with timestamp
    * @param msg 
    */
    public static void printLog(String msg){
       System.out.println( "<" + Calendar.getInstance().getTime().toString() + "> " + msg);
    }
    
    public static void dbg1(int loglevel, String msg){
       if((loglevel & DBG1) == DBG1) System.out.println( "[DBG1]: <" + Calendar.getInstance().getTime().toString() + "> " + msg);
    }
    
    public static void dbg(int loglevel, String msg){
       if((loglevel & DBG) == DBG) System.out.println( "[DBG]: <" + Calendar.getInstance().getTime().toString() + "> " + msg);
    }
    
    public static void err(int loglevel, String msg){
       if((loglevel & ERR) == ERR) System.out.println( "[ERR]: <" + Calendar.getInstance().getTime().toString() + "> " + msg);
    }
    
    public static void wrn(int loglevel, String msg){
       if((loglevel & WRN) == WRN) System.out.println( "[WRN]: <" + Calendar.getInstance().getTime().toString() + "> " + msg);
    }
    
    public static void inf(int loglevel, String msg){
       if((loglevel & INF) == INF) System.out.println( "[INF]: <" + Calendar.getInstance().getTime().toString() + "> " + msg);
    }
    /**
     * Generate pseudo random integers in diapason[min,max]
     * @param min
     * @param max
     * @return 
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max, long seed) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random(seed);

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = (int) (rand.nextInt((max - min) + 1)) + min;

        return randomNum;
    }
    public static int randInt(int min, int max, Random rand) {

        // Usually this can be a field rather than a method variable
        //Random rand = new Random(seed);

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = (int) (rand.nextInt((max - min) + 1)) + min;

        return randomNum;
    }
    
    public static int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = (int) (rand.nextInt((max - min) + 1)) + min;

        return randomNum;
    }
    
   /**
    * Returns a psuedo-random number between min and max, inclusive.
    * The difference between min and max can be at most
    * <code>Integer.MAX_VALUE - 1</code>.
    *
    * @param min Minimim value
    * @param max Maximim value.  Must be greater than min.
    * @return Integer between min and max, inclusive.
    * @see java.util.Random#nextDouble()
    */
    public static long randLong(long min, long max, long seed) {

       // Usually this can be a field rather than a method variable
       Random rand = new Random(seed);

       // nextInt is normally exclusive of the top value,
       // so add 1 to make it inclusive
       long randomNum = (long) (rand.nextDouble()*((max - min) + 1)) + min;

       return randomNum;
   }
    
   public static long randLong(long min, long max, Random rand) {

       // Usually this can be a field rather than a method variable
       //Random rand = new Random(seed);

       // nextInt is normally exclusive of the top value,
       // so add 1 to make it inclusive
       long randomNum = (long) (rand.nextDouble()*((max - min) + 1)) + min;

       return randomNum;
   }
   public static long randLong(long min, long max) {

       // Usually this can be a field rather than a method variable
       Random rand = new Random();

       // nextInt is normally exclusive of the top value,
       // so add 1 to make it inclusive
       long randomNum = (long) (rand.nextDouble()*((max - min) + 1)) + min;

       return randomNum;
   }
   
    /**
     * 
     * @param n
     * @return
     * @deprecated
     *  Reason: missing or unknown
     */
    //@Deprecated
    public static int generateRandom(int n) {
        Random random = new Random();

        if (n == 1) {   
            if (generateRandom(50) > 50) {
                return 1;
            }
            return 0;
        }
        return Math.abs(random.nextInt()) % n;
    }
    
   /**
     * Parses external ip from websites
     * @return string
     *              if ok
     *          null
     *              is smth went wrong
     */
    public static String getExternalIp() {
        String IP_GETTER1_URL = "http://api.externalip.net/ip";
        //String IP_GETTER2_URL = "http://checkip.amazonaws.com/";
        
        BufferedReader reader = null;
        String line = "";
        int tries = 0;
        do {	
            try {
                    reader = new BufferedReader(new InputStreamReader(new URL(IP_GETTER1_URL).openStream()));
                    line = reader.readLine();
                    
            } catch (FileNotFoundException fne) {
                    err(LOG_LEVEL, "Functions[getExternalIp]: File not found for url: " + IP_GETTER1_URL);
                    return null;
                    
            } catch (IOException ioe) {
                    err(LOG_LEVEL, "Functions[getExternalIp]: Got IO Exception, tries = " + (tries + 1)+"; Message: " + ioe.getMessage());
                    tries++;
                    try {
                            Thread.sleep(300000);
                    } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                    }
                    //continue;
                    
            } catch (Exception exc) {
                    exc.printStackTrace(System.out);
            }
        } while (reader == null && tries < 5);

        if (line != null && line.length() > 0) {
            dbg(LOG_LEVEL, "Functions[getExternalIp]: Your external ip address is: " + line);
        
        }  else {
            dbg(LOG_LEVEL, "Functions[getExternalIp]: couldn't get your ip address");
            line="";
        }

        return line;
    }
    
    public static String humanDayOfWeek(int DefaultDayOfWeek){
        if(DefaultDayOfWeek>0) DefaultDayOfWeek -= 1;
        String[] namesOfDays = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        return namesOfDays[DefaultDayOfWeek];        
    }
    
    public static int humanDayOfWeek(String DefaultDayOfWeek){
        
        if(DefaultDayOfWeek.equalsIgnoreCase("MON")) return 1;
        else if(DefaultDayOfWeek.equalsIgnoreCase("TUE")) return 2;
        else if(DefaultDayOfWeek.equalsIgnoreCase("WED")) return 3;
        else if(DefaultDayOfWeek.equalsIgnoreCase("THU")) return 4;
        else if(DefaultDayOfWeek.equalsIgnoreCase("FRI")) return 5;
        else if(DefaultDayOfWeek.equalsIgnoreCase("SAT")) return 6;
        else if(DefaultDayOfWeek.equalsIgnoreCase("SUN")) return 7;
        
        return 0;
        
    }
    
    public static int subtractHumanDay(Calendar startDay, Calendar endDay){
        int start, end;
        start = humanDayOfWeek(humanDayOfWeek(startDay.get(Calendar.DAY_OF_WEEK)));
        end = humanDayOfWeek(humanDayOfWeek(endDay.get(Calendar.DAY_OF_WEEK)));
        if(start > end) return (7 - (start - end));
        else return (end - start);
    }
    
    public static String arrayToString(Object[] s, String glue) {
        int k = s.length;
        if (k == 0)
            return null;
        StringBuilder out = new StringBuilder();
        out.append(s[0].toString());
        for (int x = 1; x < k; ++x) {
            out.append(glue).append((s[x] != null) ? s[x].toString() : " ");
        }

        return out.toString();
    }    
    
    /**
     * Created by fedor.belov on 21.08.13.
     * http://stackoverflow.com/questions/1155107/is-there-a-cross-platform-java-method-to-remove-filename-special-chars
     * As suggested elsewhere, this is not usually what you want to do. 
     * It is usually best to create a temporary file using a secure method such as File.createTempFile().
     * You should not do this with a whitelist and only keep 'good' characters. 
     * If the file is made up of only Chinese characters then you will strip everything out of it.
     *  We can't use a whitelist for this reason, we have to use a blacklist.
     *  Linux pretty much allows anything which can be a real pain. 
     *  I would just limit Linux to the same list that you limit Windows 
     *  to so you save yourself headaches in the future.
     *  Using this C# snippet on Windows I produced a list of characters that are not valid on Windows. 
     *  There are quite a few more characters in this list than you may think (41) 
     *  so I wouldn't recommend trying to create your own list.
     */
    private final static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};
    static {
        Arrays.sort(illegalChars);
    }
    public static String cleanIllegalChars(String badFileName) {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++) {
            int c = (int)badFileName.charAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.append((char)c);
            }
        }
        return cleanName.toString();
    }
    
    public static void clearBuffer(InputStream is) {
		int avail;
		try {
			avail = is.available();
			is.skip(avail);		
		} catch (IOException e) {
		}
	}

}
