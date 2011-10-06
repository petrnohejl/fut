/**
 *
 */
package fut.server.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class provides basic cryptographic functionality, such as MD5 & SHA-1
 * hash codes and custom simple-hash routine.

 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 5.3.2010
 */
public final class Crypto {
    private static final String HEXES = "0123456789abcdef";
    private static StringBuilder hexer = null;
    private static MessageDigest md5 = null;
    private static MessageDigest sha1 = null;


    private Crypto() {
    }


    public static String getHex( byte [] data ) {
      if ( data == null ) {
	  throw new NullPointerException("Input data can not be null !");
      }
      if (hexer == null) {
	  hexer = new StringBuilder(1024);
      }

      hexer.delete(0, hexer.capacity());
      hexer.ensureCapacity(data.length * 2);

     for (byte b : data ) {
        hexer.append(HEXES.charAt((b & 0xF0) >> 4))
             .append(HEXES.charAt((b & 0x0F)));
      }
      return hexer.toString();
    }

    public static String getSHA1Hash(String s) throws NoSuchAlgorithmException {
	if (sha1 == null)  sha1 = MessageDigest.getInstance("sha1");
	return getHex(compute(sha1,s));
    }


    public static String getMD5Hash(String s) throws NoSuchAlgorithmException {
	if (md5 == null)  md5 = MessageDigest.getInstance("md5");
	return getHex(compute(md5,s));
    }


    public static String getSimpleHash(String s) {
	int i = (int)(("~1N_3"+s+"$5qM0j").hashCode() + 129081);
	return Integer.toHexString(i);
    }

    private static byte[] compute(MessageDigest md, String data) {
	md.reset();
	md.update(data.getBytes(),0,data.length());
	return md.digest();
    }
}
