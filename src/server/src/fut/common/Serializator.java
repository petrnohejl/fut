/**
 *
 */
package fut.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 3.12.2010
 */
public class Serializator {

    private Serializator() {
    }

//    public static void main(String[] args) throws Exception {
//	List<String> aa = new ArrayList<String>(10);
//	aa.add("FUT:: Fut is DaBest !!!!!");
//
//	char[] bb = Serializator.save64(aa);
//	String tmp = new String(bb);
//	char[] cc = tmp.toCharArray();
//
//	System.out.println(tmp);
//	List<String> total = (List<String>) Serializator.load64(cc);
//	System.out.println(total.get(0));
//
//    }

    public static  Object load(byte[] buf) throws IOException {
	ObjectInputStream ois = null;
	Object obj;

	try {
	    ois = new ObjectInputStream(new GZIPInputStream(
		    new ByteArrayInputStream(buf), 8 * 1024));
	    obj = ois.readObject();
	} catch (ClassNotFoundException e) {
	    throw new IOException(e.getMessage());
	} finally {
	    if (ois != null) {
		try {
		    ois.close();
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
	    }
	}

	return obj;
    }

    public static byte[] save(Object obj) throws IOException {
	ObjectOutputStream oos = null;
	ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
	GZIPOutputStream zip = new GZIPOutputStream(baos, 8 * 1024);
	try {
	    oos = new ObjectOutputStream(zip);
	    oos.writeObject(obj);
	    oos.flush();
	    zip.flush();
	    zip.finish();
	    baos.flush();
	    return baos.toByteArray();
	} finally {
	    if (oos != null) {
		try {
		    oos.close();
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
	    }

	    if (zip != null) {
		try {
		    zip.close();
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
	    }

	    if (baos != null) {
		try {
		    baos.close();
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
	    }
	}
    }

    public static  Object load64(String base64) throws IOException {
	return load(Base64Coder.decode(base64));
    }

    public static  Object load64(char[] base64) throws IOException {
	return load(Base64Coder.decode(base64));
    }

    public static char[] save64(Object obj) throws IOException {
	return Base64Coder.encode(save(obj));
    }
}
