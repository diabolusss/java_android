package org.secure.sms;

import android.telephony.SmsMessage;

public class Functions{

	public static String SmsMessageToJson(SmsMessage sms){
		String result = 
				"{" +
						"\"getDisplayMessageBody\":"+sms.getDisplayMessageBody()+"\n,"+
						"\"getDisplayMessageBodyLength\":"+((sms.getDisplayMessageBody() != null)?(sms.getDisplayMessageBody().length()):(0))+"\n,"+
						"\"getDisplayOriginatingAddress\":"+sms.getDisplayOriginatingAddress()+"\n,"+
						"\"getEmailBody\":"+sms.getEmailBody()+"\n,"+
						"\"getEmailBodyLength\":"+((sms.getEmailBody() != null)?(sms.getEmailBody().length()):(0))+"\n,"+
						"\"getEmailFrom\":"+sms.getEmailFrom()+"\n,"+
						"\"getIndexOnIcc\":"+sms.getIndexOnIcc()+"\n,"+
						"\"getMessageBody\":"+sms.getMessageBody()+"\n,"+
						"\"getMessageBodyLength\":"+((sms.getMessageBody() != null)?(sms.getMessageBody().length()):(0))+"\n,"+
						"\"getOriginatingAddress\":"+sms.getOriginatingAddress()+"\n,"+
						"\"getOriginatingAddress\":"+sms.getProtocolIdentifier()+"\n,"+
						"\"getPseudoSubject\":"+sms.getPseudoSubject()+"\n,"+
						"\"getServiceCenterAddress\":"+sms.getServiceCenterAddress()+	"\n,"+					
						"\"getStatus\":"+sms.getStatus()+"\n,"+
						"\"getStatus\":"+sms.getStatusOnIcc()+"\n,"+
						"\"getTimestampMillis\":"+sms.getTimestampMillis()+"\n,"+
						"\"isEmail\":"+sms.isEmail()+"\n,"+
						"\"isStatusReportMessage\":"+sms.isStatusReportMessage()+
						
				"}"
			;
		/*
		 * {
		 * 	"getDisplayMessageBody":(error 0) {msg type not recognized 0},
		 * 	"getDisplayOriginatingAddress":colt@toshiba.gotdns.ch,
		 * 	"getEmailBody":(error 0) {msg type not recognized 0},
		 * 	"getEmailFrom":colt@toshiba.gotdns.ch,
		 * 	"getIndexOnIcc":-1,
		 * 	"getMessageBody":colt@toshiba.gotdns.ch (error 0) {msg type not recognized 0},
		 * 	"getOriginatingAddress":100,
		 * 	"getOriginatingAddress":0,
		 * 	"getPseudoSubject":,
		 * 	"getServiceCenterAddress":+37129599994,
		 * 	"getStatus":0,
		 * 	"getStatus":-1,
		 * 	"getTimestampMillis":1447102250000,
		 * 	"isEmail":true,
		 * 	"isStatusReportMessage":false
		 * }
		*/
		return result;
	}
	
	public static String convertByteArrayToHexString(byte[] arrayBytes) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < arrayBytes.length; i++) {
			stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return stringBuffer.toString();
	}

}
