package com.spots.varramie;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;


public class PDU_Factory {
	
	public static PDU touch_action(final float x, final float y, final int action){

		PDU pdu = new PDU(9);
		pdu.setByte(0, (byte) action);
		//pdu.setByte(1, (byte) 10);
		pdu.setInt(1, Math.round(x));
		pdu.setInt(5, Math.round(y));

		return pdu;
	}

	public static PDU join() {
		PDU pdu = new PDU(1);
		pdu.setByte(0, (byte) OpCodes.JOIN);
		//pdu.setByte(1, (byte) 2);
		return pdu;
	}

	public static PDU alive(final int id) {
		PDU pdu = new PDU(5);
		pdu.setByte(0, (byte) OpCodes.ALIVE);
		//pdu.setByte(1, (byte) 6);
		pdu.setInt(1, id);
		return pdu;
	}

	public static PDU quit() {
		PDU pdu = new PDU(1);
		pdu.setByte(0, (byte) OpCodes.ALIVE);
		return pdu;
	}
	
	/**
	 * Creates message PDU
	 * Client <-> Server
	 * Used for transferring a string of text
	 * @param msgType Type of message, check MsgTypes class for available types.
	 * @param text Message in form of a string.
	 * @param cryptKey Encryption key.
	 * @return message PDU
	 * @throws WrongCryptTypeException If the encryption key is unsupported.
	 * @throws UnsupportedEncodingException If the named charset is not supported
	 * @throws IOException If an I/O error has occurred.
	 */
	public static PDU message(int msgType, String text) throws WrongCryptTypeException, UnsupportedEncodingException, IOException{
		byte[] message;
		
		switch (msgType) {
		case MsgTypes.TEXT:
			text = appendZeros(text);
			message = text.getBytes("UTF-8");
			break;
			
		default:
			throw new WrongCryptTypeException();
			
		}
		
		
		PDU pdu = new PDU(12 + message.length);
		pdu.setByte(0, (byte) OpCodes.MESSAGE);
		pdu.setByte(1, (byte) msgType);
		pdu.setByte(2, (byte) '\0');
		pdu.setByte(3, (byte) '\0' );
		pdu.setShort(4, (short) message.length);
		pdu.setSubrange(6, new byte[] {'\0', '\0'});
		pdu.setSubrange(8, new byte[] {'\0', '\0', '\0', '\0'});
		pdu.setSubrange(12, message);
		pdu.setByte(3, Checksum.calc(pdu.getBytes(), pdu.length()));

		return pdu;
	}
	
	/**
	 * Calculates a new checksum of a PDU and compares it to 
	 * the existing checksum in the PDU.
	 * @param pdu The PDU that is going to be checked.
	 * @param index The position in the PDU of were the existing checksum is.
	 * @return Whatever if the checksum matches.
	 */
	public static boolean checksum(PDU pdu, int index){
		byte orgChecksum = (byte) pdu.getByte(index);
		pdu.setByte(index, (byte) '\0');
		byte newChecksum = Checksum.calc(pdu.getBytes(), pdu.length() );
		pdu.setByte(index, (byte) orgChecksum);
		return orgChecksum == newChecksum;	
	}

	
	/**
	 * Appends \0 to each string and event out
	 * the string to 4 bytes.
	 * @param strArray 
	 * @return A array of strings with \0
	 */
	private static String appendZeros(String[] strArray) {
		String returnStr = "";
		for(String str : strArray)
			returnStr +=  (str + "\0");
		
		while (returnStr.length()%4 != 0)
			returnStr += "\0";
		
		return returnStr;
	}
	
	/**
	 * Appends \0 to the string and event out
	 * the string to 4 bytes.
	 * @param str 
	 * @return A strings with \0
	 */
	public static String appendZeros(String str) {
		str += "\0";
		
		while (str.length()%4 != 0)
			str += "\0";
		
		return str;
	}
	
	/**
	 * Appends \0 to the byte array so that its length is evenly devided by 4.
	 * @param
	 * @return
	 */
	public static byte[] appendZeros(byte[] bytes){
		
		int appendLength =  4 - (bytes.length  % 4);
		byte[] b = new byte[bytes.length + appendLength];
		
		
		for(int i = 0; i < bytes.length; i++ ){
			b[i] = bytes[i];
		}

		for(int j = 0; j < appendLength; j++){
			b[j+bytes.length] = '\0';
		}		
		
		
		return b;
	}
	
	/**
	 * Devides a string into a array of strings with '\0' as token.
	 * @param str
	 * @return
	 */
	public static String[] removeZeros(String str) {
		return str.split("\0");
	}
	
	/**
	 * Removes zeroes from an array of bytes.
	 * @param bytes
	 * @return new array of bytes without the zeroes.
	 */
	public static byte[] removeZeros(byte[] bytes) {

		int counter = 0;
		while(bytes[bytes.length-counter-1] == '\0'){
			counter++;
		}
		
		if (counter == 0) {
			return bytes;
		}
		
		byte[] newBytes = new byte[bytes.length-counter];
		
		for (int i = 0; i < bytes.length-counter; i++) {
			newBytes[i] = bytes[i];
		}
		return newBytes;
	}
	
}
