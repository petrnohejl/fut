package fut.server.net;

import java.util.UnknownFormatFlagsException;

import fut.server.exception.ParserException;
import fut.server.net.Protocol.*;

//TODO: Implement getMethod and getSubject to arrays

/**
 * 
 * @author jlibosva
 *
 */
public class Parser {
	
	/**
	 * 
	 * @param src
	 * @param key
	 * @param count
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	private static int find(byte[] src, byte key, int count) throws IndexOutOfBoundsException {
		int cnt = 1;
		int i = 0;
		
		if (count < 1) 
			throw new IndexOutOfBoundsException("The count variable in method Parser.find has to be greater than 0.");
		
		for (; i < src.length; i++) {
			if (src[i] == key) {
				if (count == cnt) {
					break;
				}
				cnt++;
			}
		}
		
		if (i == src.length)
			throw new IndexOutOfBoundsException("The key " + key + " has not been found in private method Parser.find.");
		
		return i;
	}
	
	/**
	 * Removes ID from the stream and returns data
	 * @param stream Data stream
	 * @return New data without ID
	 */
	public static byte[] removeID(byte[] stream) {
		byte[] ret;
		int i;
		int length;
		
		i = find(stream,(byte)Protocol.SEPARATOR,1);
		length = stream.length-i-1;
		
		ret = new byte[length];
		
		System.arraycopy(stream, i+1, ret, 0, length);
		
		return ret;
	}
	
	/**
	 * 
	 * @param stream
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws UnknownFormatFlagsException
	 */
	public static String getID(byte[] stream) throws IndexOutOfBoundsException, ParserException {
		byte[] IDB;
		int i=0;
		
		i = find(stream,(byte)Protocol.SEPARATOR,1);
		
		IDB = new byte[i];
		
		System.arraycopy(stream, 0, IDB, 0, i);
		
		return new String(IDB);
	}
	
	
	/**
	 * 
	 * @param stream
	 * @return
	 */
	public static String getUsername(byte[] stream) {
		byte[] subB;
		int begin;
		int length;
		
		begin = find(stream,(byte)Protocol.SEPARATOR,1) + 1;
		length = find(stream,(byte)Protocol.SEPARATOR,2) - begin;
		
		subB = new byte[length];
		
		System.arraycopy(stream, begin, subB, 0, length);
		
		return new String(subB);
	}
	
	/**
	 * 
	 * @param stream The input stream from socket
	 * @return Method to be called on server
	 * @throws IndexOutOfBoundsException When separator is not found in input stream
	 * @throws UnknownFormatFlagsException Thrown when method is not known.
	 */
	public static METHOD getMethod(byte[] stream) throws IndexOutOfBoundsException, ParserException {
		byte[] subB;
		String sub;
		int i;
		
		i = find(stream,(byte)Protocol.SEPARATOR,1);
		
		subB = new byte[i];
		
		System.arraycopy(stream, 0, subB, 0, i);
		
		sub = new String(subB);
		
		if (sub.compareTo(Protocol.GET) == 0) {
			return METHOD.GET;	
		}
		
		if (sub.compareTo(Protocol.SAVE) == 0) {
			return METHOD.SAVE;	
		}
		
		if (sub.compareTo(Protocol.ACK) == 0) {
			return METHOD.ACK;	
		}
		
		if (sub.compareTo(Protocol.ERROR) == 0) {
			return METHOD.ERROR;	
		}
		
		if (sub.compareTo(Protocol.PING) == 0) {
			return METHOD.PING;
		}
		
		if (sub.compareTo(Protocol.REG) == 0) {
			return METHOD.REG;
		}
		
		throw new ParserException("getMethod: Unknown method " + sub);
	}
	
	/**
	 * 
	 * @param stream
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws UnknownFormatFlagsException
	 */
	public static SUBJECT getSubject(byte[] stream) throws IndexOutOfBoundsException, ParserException {
		byte[] subB;
		String sub;
		int begin;
		int length;
		
		begin = find(stream,(byte)Protocol.SEPARATOR,1) + 1;
		length = find(stream,(byte)Protocol.SEPARATOR,2) - begin;
		
		subB = new byte[length];
		
		System.arraycopy(stream, begin, subB, 0, length);
		
		sub = new String(subB);
		
		if (sub.compareTo(Protocol.IMEI) == 0) {
			return SUBJECT.IMEI;	
		}
		
		if (sub.compareTo(Protocol.IMSI) == 0) {
			return SUBJECT.IMSI;	
		}
		
		if (sub.compareTo(Protocol.MCCMNC) == 0) {
			return SUBJECT.MCCMNC;	
		}
		
		if (sub.compareTo(Protocol.SSE) == 0) {
			return SUBJECT.SSE;	
		}
		
		if (sub.compareTo(Protocol.LONG) == 0) {
			return SUBJECT.LONG;	
		}
		
		if (sub.compareTo(Protocol.LAT) == 0) {
			return SUBJECT.LAT;	
		}
		
		if (sub.compareTo(Protocol.TIME) == 0) {
			return SUBJECT.TIME;	
		}
		
		if (sub.compareTo(Protocol.SRC) == 0) {
			return SUBJECT.SRC;	
		}
		
		if (sub.compareTo(Protocol.ALT) == 0) {
			return SUBJECT.ALT;	
		}
		
		if (sub.compareTo(Protocol.AZIM) == 0) {
			return SUBJECT.AZIM;	
		}
		
		if (sub.compareTo(Protocol.SPD) == 0) {
			return SUBJECT.SPD;	
		}
		
		if (sub.compareTo(Protocol.ACC) == 0) {
			return SUBJECT.ACC;	
		}
		
		if (sub.compareTo(Protocol.STOLEN) == 0) {
			return SUBJECT.STOLEN;
		}
		
		if (sub.compareTo(Protocol.ACCOUNTS) == 0) {
			return SUBJECT.ACCOUNTS;
		}
		
		if (sub.compareTo(Protocol.CONTACTS) == 0) {
			return SUBJECT.CONTACTS;
		}
		
		if (sub.compareTo(Protocol.HISTORY) == 0) {
			return SUBJECT.HISTORY;
		}
		
		if (sub.compareTo(Protocol.DEVICE) == 0) {
			return SUBJECT.DEVICE;
		}
		
		if (sub.compareTo(Protocol.ALARM) == 0) {
			return SUBJECT.ALARM;
		}
		
		if (sub.compareTo(Protocol.GEODATA) == 0) {
			return SUBJECT.GEODATA;
		}
		
		if (sub.compareTo(Protocol.IPWD) == 0) {
			return SUBJECT.IPWD;
		}
		
		throw new ParserException("getSubject: Unknown subject " + sub);
	}
	
	/**
	 * Parses the data stream and returns the parsed content of message
	 * @param stream 
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public static byte[] getContent(byte[] stream) throws IndexOutOfBoundsException {
		byte[] content;
		int begin;
		int length;
		
		begin = find(stream,(byte)Protocol.SEPARATOR,2) + 1;
		length = find(stream,(byte)Protocol.SEPARATOR,3) - begin;
		
		content = new byte[length];
		
		System.arraycopy(stream, begin, content, 0, length);
		
		return content;
	}
	
	/**
	 * 
	 * @param stream
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public static String getContentString(byte[] stream) throws IndexOutOfBoundsException {
		return new String(getContent(stream));
	}
	
	/**
	 * 
	 * @param stream
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public static String getOptional(byte[] stream) throws IndexOutOfBoundsException {
		byte[] optB;
		int begin = find(stream,(byte)Protocol.SEPARATOR,3) + 1;
		int length = stream.length - begin;
		
		optB = new byte[length];
		
		System.arraycopy(stream, begin, optB, 0, length);
		
		return new String(optB);
	}
}
