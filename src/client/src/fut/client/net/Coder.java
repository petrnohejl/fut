package fut.client.net;


import java.util.Arrays;

/**
 * 
 * @author jlibosva
 *
 */
public class Coder {	
	/**
	 * Class for work with position of the character wanted by coder
	 * @author jlibosva
	 *
	 */
	private class Key {
		/**
		 * Position of the character that will be used for coding
		 */
		private int pos;
		
		/**
		 * The coding key
		 */
		private byte[] key;
		
		/**
		 * Constructor
		 * @param length Length of the key string
		 */
		public Key(final String key) {
			this.key = key.getBytes();
			this.pos = -1;
		}
		
		/**
		 * Counts up a new position in key string
		 * @return Position used for coding
		 */
		public byte next() {
			pos++;
			pos = pos % key.length;
			
			return key[pos];
		}
		
		public int getPos() {
			return pos;
		}
		
		public byte getByte() {
			return key[pos];
		}
		
		/**
		 * Re-initializes the key calss
		 */
		public void reset() {
			pos = -1;
		}
		
		/**
		 * Gets length of the key
		 * @return Length of the key
		 */
		public int getKeyLength() {
			return key.length;
		}
		
		/**
		 * Gets the key
		 * @return Key
		 */
		public byte[] getKey() {
			return key;
		}
	}
	
	/**
	 * The position of key character used for coding
	 */
	private Key key;
	
	/**
	 * 
	 * @param key Key for encoding/decoding
	 */
	public Coder(String key) {
		this.key = new Key(key);
	}
	
	
	/**
	 * 
	 * @param message
	 * @return
	 */
	public byte[] encode(byte[] message) {
		byte[] data = new byte[message.length + key.getKeyLength()];
		byte[] result = new byte[data.length];
		
		System.arraycopy(message, 0, data, 0, message.length);
		System.arraycopy(key.getKey(), 0, data, message.length, key.getKeyLength());
		
		System.out.println("Encoding main message with length " + message.length + " and adding key with length " + key.getKeyLength());
		
		key.reset();
		
		for(int i=0; i<result.length; i++) {
			if ((i % 2) == 0) {
				result[i] = (byte) ((data[i] + key.next()) % 256);
			}
			else {
				result[i] = (byte) ((data[i] - key.next() + 256) % 256);
			}	
		}
		
		return result;
	}
	
	/**
	 * Decodes given message
	 * @param message Encoded message to be decoded
	 * @return Decoded byte array if decode has been successful, null otherwise
	 */
	public byte[] decode(byte[] message) {
		byte[] data = new byte [message.length];
		byte[] result = new byte[message.length - key.getKeyLength()];
		byte[] keyF = new byte[key.getKeyLength()];
		
		key.reset();
		
		for(int i=0; i<data.length; i++) {
			if ((i % 2) == 0) {
				data[i] = (byte) ((message[i] - key.next() + 256) % 256);
			}
			else {
				data[i] = (byte) ((message[i] + key.next() + 256) % 256);
			}	
		}
		System.out.println("Decoding main message with length " + result.length + " and removing key with length " + key.getKeyLength());
		System.arraycopy(data, message.length - key.getKeyLength(), keyF, 0, key.getKeyLength());
		if (Arrays.equals(key.getKey(), keyF)) {
			System.arraycopy(data, 0, result, 0, message.length - key.getKeyLength());
			return result;	
		} else {
			System.out.println("Original key: " + new String(key.getKey()));
			System.err.println("Decoded key: " + new String(keyF));
			
		}
		
		return null;
	}
	
	
	/**
	 * 
	 * @param message
	 * @return
	 */
	@Deprecated
	public String encode(String message) {
		StringBuffer result = new StringBuffer();
		char c;
		
		key.reset();
		
		for(int i=0; i<message.length(); i++) {
			if ((i % 2) == 0) {
				c = (char)((message.codePointAt(i) + key.next()) % 256);
//				System.out.println("Key " + key.charAt(keyPos.getPos()) + " = " + key.codePointAt(keyPos.getPos()));
//				System.out.println("val: " + val);
//				System.out.println("c: " + c);
				result.append(c);
			}
			else {
				c = (char)((message.codePointAt(i) - key.next() + 256) % 256);
//				System.out.println("Key " + key.charAt(keyPos.getPos()) + " = " + key.codePointAt(keyPos.getPos()));
//				System.out.println("val: " + val);
//				System.out.println("c: " + c);
				result.append(c);
			}	
		}
		
		return result.toString();
	}
	
	/**
	 * 
	 * @param message
	 * @return
	 */
	@Deprecated
	public String decode(String message) {
		StringBuffer result = new StringBuffer();
		char c;
		
		key.reset();
		
		for(int i=0; i<message.length(); i++) {
			if ((i % 2) == 0) {
				c = (char)((message.codePointAt(i) - key.next() + 256) % 256);
				result.append(c);
			}
			else {
				c = (char)((message.codePointAt(i) + key.next() + 256) % 256);
				result.append(c);
			}
		}
		
		return result.toString();
	}
}
