package org.rusak.securemessanger.constants;

public class SMSConstants {
	// All available column names in SMS table
    // [_id, thread_id, address, 
	// person, date, protocol, read, 
	// status, type, reply_path_present, 
	// subject, body, service_center, 
	// locked, error_code, seen]	
	public static final String EXTRA_NAME = "pdus";
	public static final String STORAGE_URI = "content://sms";
	public static final String STORAGE_URI_INBOX = STORAGE_URI+"/inbox";
	
	public static final String ID 		= "_id";
	public static final String ADDRESS 		= "address";
	public static final String THREAD_ID 		= "thread_id";
    public static final String PERSON 		= "person";
    public static final String DATE 			= "date";
    public static final String DATE_SENT		= "date_sent";
    public static final String READ 			= "read";
    public static final String STATUS 		= "status";
    public static final String TYPE 			= "type";
    public static final String BODY 			= "body";
    public static final String SEEN 			= "seen";
    public static final String SUBJECT 		= "subject";
    public static final String SERVICE_CENTER = "service_center";
    
    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;
    
    public static final int MESSAGE_IS_NOT_READ = 0;
    public static final int MESSAGE_IS_READ = 1;
    
    public static final int MESSAGE_IS_NOT_SEEN = 0;
    public static final int MESSAGE_IS_SEEN = 1;
}
