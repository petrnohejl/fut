package fut.android.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.content.Context;

public class Storage {

	private FileInputStream fileInput;
	private Context context;
	private String fileName = "storage";

	/**
	 * Konstruktor
	 * 
	 * @param context
	 *            Pouzity kontext, obvykle objekt tridy Application nebo
	 *            Activity
	 */
	public Storage(Context context) {
		this.context = context;
	}

	/**
	 * Nacte obsah souboru do promennych tridy
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws StreamCorruptedException
	 */
	public DataObject loadPreferences() throws IOException {
		ObjectInputStream ois = null;
		DataObject obj = null;

		try {
			ois = new ObjectInputStream(new GZIPInputStream(
					context.openFileInput(fileName), 8 * 1024));
			obj = (DataObject) ois.readObject();
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

	public void savePreferences(DataObject obj) throws IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new GZIPOutputStream(
					context.openFileOutput(fileName, Context.MODE_PRIVATE),
					8 * 1024));
			oos.writeObject(obj);
			oos.flush();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public File getFileLocation() {
		return context.getFileStreamPath(fileName);
	}
	
	public boolean deletePreferences() {
		return context.deleteFile(fileName);
	}

	/**
	 * Kontrola existence souboru pro ukladani nastaveni
	 * 
	 * @return Existuje soubor pro ulozeni nastaveni?
	 */
	public boolean fileExist() {
		try {
			fileInput = context.openFileInput(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return fileInput != null ;
	}
}
