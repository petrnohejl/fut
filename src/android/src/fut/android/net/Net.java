package fut.android.net;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import fut.android.net.Parser;
import fut.android.net.Protocol;
import fut.android.net.ParserException;
import fut.android.net.InvalidUserException;
import fut.android.net.Net;


public class Net {
	/**
	 * Coder for current connection.
	 */
	private Coder coder;
	
	/**
	 * Socket for current connection.
	 */
	private Socket socket;
	
	/**
	 * Server IP or DNS name
	 */
	private String server;
	
	/**
	 * Port for running service on server
	 */
	private int port;
	
	/**
	 * This is for controlling wheter connection is opened. 
	 */
	private boolean isConnected;
	
	private final int SIZE = 512;
	
	/**
	 * 
	 * @param address
	 * @param port
	 * @param password
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Net(String address, int port, String password) {
		this.server = address;
		this.port = port;
		isConnected = false;
		coder = new Coder(password);
		Log.d("FUT", "Created new Net class");
	}
	
	private List<byte[]> makeList(byte[] content) {
		int count = content.length / SIZE;
		int rest = content.length % SIZE;
		List<byte[]> list = new ArrayList<byte[]>(count);
		byte[] tmp;
		
		int i = 0;
		for(; i<count; i++) {
			tmp = new byte[SIZE];
			System.arraycopy(content, i*SIZE, tmp, 0, SIZE);
			list.add(tmp);
		}
		
		tmp = new byte[rest];
		System.arraycopy(content, i*SIZE, tmp, 0, rest);
		list.add(tmp);
		
		return list;
	}
	
	private byte[] makeByteFromList(List<byte[]> list) {
		int count = list.size() - 1;
		int rest = list.get(count).length;
		byte[] bytes = new byte[count*SIZE+rest];
		
		int i = 0;
		for(;i<count;i++) {
			System.arraycopy(list.get(i), 0, bytes, i*SIZE, SIZE);
		}
		System.arraycopy(list.get(i), 0, bytes, i*SIZE, rest);
		
		return bytes;
	}
	
	public boolean isConnected() {
		return this.isConnected;
	}
	
	/**
	 * Creates new socket for communication
	 * @return True on error, false otherwise
	 */
	public void connect(String id) throws UnknownHostException, IOException, InvalidUserException{
		try {
			if (!isConnected) {
				socket = new Socket(server, port);
				isConnected = true;
				if (ping(id)) {
					isConnected = false;
					throw new InvalidUserException("Bad username or password");
				}
				Log.d("FUT", "connect - Android is connected");
			}
		}
		catch (UnknownHostException e) {
			Log.e("FUT", "connect - UnknownHostException: " + e.getMessage());
			throw e;
		}
		catch (IOException e) {
			Log.e("FUT", "connect - IOException: " + e.getMessage());
			throw e;
		}
	}
	
	private boolean ping(String id) {
		byte[] stream = (Protocol.PING + Protocol.SEPARATOR + Protocol.SEPARATOR + Protocol.SEPARATOR).getBytes();
		System.err.println("saveGeneral stream(without id$) length: " + stream.length);
		byte[] coded = coder.encode(stream);
		byte[] header = (id + Protocol.SEPARATOR).getBytes();
		byte[] data = new byte[header.length + coded.length];

		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(coded, 0, data, header.length, coded.length);

		System.out.println("ping data length: " + data.length);

		System.out.println("Sending ping content: " + id + Protocol.SEPARATOR + new String(stream));
		//System.err.println("Sending ping encoded: " + new String(data));

		try {
			send(data);
			if (getAck()) {
				return false;
			} else {
				System.err.println(Net.class.getName() + " - send Sending has failed.");
				return true;
			}
		} catch (IOException e) {
			System.err.println(Net.class.getName() + " - send IOException: " + e.getMessage());
			e.printStackTrace();
			return true;
		}
	}

	/**
	 * 
	 * @param data
	 * @throws IOException
	 */
	private void send(byte[] data) throws IOException {		
		if (!isConnected) {
			Log.e("FUT", "send - Sending data but not connected");
			return;
		}
//		Log.d("FUT", "send - Message: " + new String(data));
		Log.d("FUT", "send - Message length: " + data.length);
		socket.getOutputStream().write(data);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Log.d("FUT", "Encoded-Decoded message: " + new String(coder.decode(coder.encode(data))));
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		if (!isConnected) {
			Log.e("FUT", "Disconnecting but not connected");
			return; 
		}
		Log.d("FUT", "Closing socket");
		socket.close();
		isConnected = false;
		Log.d("FUT", "Socket closed");
	}

	
	/**
	 * Sends an IMEI to the server for storing into the database
	 * @id ID of user
	 * @param imei IMEI of the phone
	 * @return 0 on success
	 */
	public int sendIMEI(String id, String imei) {
		return saveGeneral(id, Protocol.IMEI, imei);
	}
	

	/**
	 * Sends an IMSI to the server for storing into the database 
	 * @param id ID of user
	 * @param imsi IMSI of the phone
	 * @return 0 on success
	 */
	public int sendIMSI(String id, String imsi) {
		return saveGeneral(id, Protocol.IMSI, imsi);
	}
	
	/**
	 * Sends a MCC and MNC to the serve for storing into the database
	 * @param id ID of user
	 * @param mccmnc MCC + MNC of the SIM
	 * @return 0 on success
	 */
	public int sendMCCMNC(String id, String mccmnc) {
		return saveGeneral(id, Protocol.MCCMNC, mccmnc);
	}
	
	/**
	 * Sends a SIM serial number to the serve for storing into the database
	 * @param id ID of user
	 * @param sim SIM serial no
	 * @return 0 on succes
	 */
	public int sendSSE(String id, String sim) {
		return saveGeneral(id, Protocol.SSE, sim);
	}
	
	/**
	 * Sends source of the GPS position for storing into the database
	 * @param id ID of user
	 * @param src source type
	 * @return 0 on succes
	 */
	public int sendSRC(String id, String src) {
		return saveGeneral(id, Protocol.SRC, src);
	}
	
	/**
	 * Sends latitude for storing into the database
	 * @param id ID of user
	 * @param lat latitude
	 * @return 0 on succes
	 */
	public int sendLAT(String id, String lat) {
		return saveGeneral(id, Protocol.LAT, lat);
	}
	
	/**
	 * Sends longitude for storing into the database
	 * @param id ID of user
	 * @param longi longitude
	 * @return 0 on succes
	 */
	public int sendLONG(String id, String longi) {
		return saveGeneral(id, Protocol.LONG, longi);
	}
	
	/**
	 * Sends altitude for storing into the database
	 * @param id ID of user
	 * @param alt altitude
	 * @return 0 on succes
	 */
	public int sendALT(String id, String alt) {
		return saveGeneral(id, Protocol.ALT, alt);
	}
	
	/**
	 * Sends azimuth for storing into the database
	 * @param id ID of user
	 * @param azim azimuth
	 * @return 0 on succes
	 */
	public int sendAZIM(String id, String azim) {
		return saveGeneral(id, Protocol.AZIM, azim);
	}
	
	/**
	 * Sends speed for storing into the database
	 * @param id ID of user
	 * @param spd speede
	 * @return 0 on succes
	 */
	public int sendSPD(String id, String spd) {
		return saveGeneral(id, Protocol.SPD, spd);
	}
	
	/**
	 * Sends accuracy for storing into the database
	 * @param id ID of user
	 * @param acc accuracy
	 * @return 0 on succes
	 */
	public int sendACC(String id, String acc) {
		return saveGeneral(id, Protocol.ACC, acc);
	}
	
	/**
	 * Sends timestamp for storing into the database
	 * @param id ID of user
	 * @param time timestamp
	 * @return 0 on succes
	 */
	public int sendTIME(String id, String time) {
		return saveGeneral(id, Protocol.TIME, time);
	}
	
	private int sendFile(String id, String subject, String file) {
		byte[] encoded = coder.encode(file.getBytes());
		List<byte[]> list = makeList(encoded);
		String count = Integer.toString(list.size());
		int val = saveGeneral(id, subject, count);
		if (val != 0)
			return val;
		try {
			for (byte[] arr : list) {
				Log.d("FUT", "Sending array of bytes");
				send(arr);
			}

			if (getAck()) {
				return 0;
			} else {
				Log.e("FUT", "Sending has failed.");
				return 1;
			}
		} catch (IOException e) {
			Log.e("FUT", "IOException while sending array of file: " + e.getMessage());
		}
		
		return 0;
	}
	
	/**
	 * 
	 * @param id
	 * @param accountsFile
	 * @return
	 */
	public int sendAccounts(String id, String accountsFile) {
		return sendFile(id, Protocol.ACCOUNTS, accountsFile);
	}
	
	/**
	 * 
	 * @param id
	 * @param contactsFile
	 * @return
	 */
	public int sendContacts(String id, String contactsFile) {
		return sendFile(id, Protocol.CONTACTS, contactsFile);
	}
	
	/**
	 * 
	 * @param id
	 * @param historyFile
	 * @return
	 */
	public int sendHistory(String id, String historyFile) {
		return sendFile(id, Protocol.HISTORY, historyFile);
	}
	
	
	/**
	 * Generaly creates and sends data stream
	 * @param id ID of the user
	 * @param subject Subject of message
	 * @param content Content of message
	 * @return 0 on success
	 */
	private int saveGeneral(String id, String subject, String content) {
		byte[] stream = (Protocol.SAVE + Protocol.SEPARATOR + subject + Protocol.SEPARATOR + content + Protocol.SEPARATOR).getBytes();
		Log.d("FUT", "stream(without id$) length: " + stream.length);
		byte[] coded = coder.encode(stream);
		byte[] header = (id + Protocol.SEPARATOR).getBytes();
		byte[] data = new byte[header.length + coded.length];
		
		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(coded, 0, data, header.length, coded.length);
		
		Log.d("FUT", "data length: " + data.length);
		
		Log.d("FUT", "Sending: " + id + Protocol.SEPARATOR + new String(stream));
//		Log.d("FUT", "encoded: " + new String(data));
		
		try {
			send(data);
			if (getAck(content)) {
				return 0;
			} else {
				Log.e("FUT", "Sending has failed.");
				return 1;
			}
		} catch (IOException e) {
			Log.e("FUT","IOException: " + e.getMessage());
			e.printStackTrace();
			return 1;
		}
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public boolean getStolen(String id) {
		String stolen = getGeneral(id, Protocol.STOLEN);
		
		if (stolen.compareToIgnoreCase("true") == 0) {
			return true;
		} else if (stolen.compareToIgnoreCase("false") == 0) {
			return false;
		} else {
			Log.e("FUT", "Unknown stolen status " + stolen);
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public boolean getAlarm(String id) {
		String alarm = getGeneral(id, Protocol.ALARM);
		
		if (alarm.compareToIgnoreCase("true") == 0) {
			return true;
		} else if (alarm.compareToIgnoreCase("false") == 0) {
			return false;
		} else {
			Log.e("FUT", "Unknown alarm status " + alarm);
		}
		
		return false;
	}
	
	/**
	 * General method for getting data
	 * @param id User's ID
	 * @param subject Subject user wants to get
	 * @return 0 on success, >0 otherwise
	 */
	private String getGeneral(String id, String subject) {
		byte[] stream = (Protocol.GET + Protocol.SEPARATOR + subject + Protocol.SEPARATOR +  Protocol.SEPARATOR).getBytes();
		Log.d("FUT", "stream(without id$) length: " + stream.length);
		byte[] coded = coder.encode(stream);
		byte[] header = (id + Protocol.SEPARATOR).getBytes();
		byte[] data = new byte[header.length + coded.length];
		
		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(coded, 0, data, header.length, coded.length);
		
		Log.d("FUT", "data length: " + data.length);
		
		Log.d("FUT", "content: " + id + Protocol.SEPARATOR + new String(stream));
		Log.d("FUT", "encoded: " + new String(data));
		
		try {
			send(data);
			
			return getResponse(subject);
				
		} catch (IOException e) {
			Log.e("FUT","IOException: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	private String getResponse(String subject) {
		if (isConnected) {
			try {
				byte[] data = getData();
				switch(Parser.getMethod(data)) {
				case ERROR: return "ERROR";
				case ACK: return Parser.getContentString(data);
					default:
				}
			} catch (IOException e) {
				Log.e("FUT","IOException: " + e.getMessage());
			} catch (IndexOutOfBoundsException e) {
				Log.e("FUT", "IndexOutOfBoundsException" + e.getMessage());
			} catch (ParserException e) {
				Log.e("FUT", e.getMessage());
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param subject Subject for comparing the data validation
	 * @param sock Communication socket
	 * @return True if everything went well, false otherwise
	 * @throws IOException When reading from socket goes wrong
	 */
	public boolean getAck(String subject) throws IOException {
		if (isConnected) {
			byte[] data = getData();
			try {
				if (Parser.getMethod(data) == Protocol.METHOD.ACK) {
					if (subject != null && subject.compareTo(Parser.getContentString(data)) == 0) {
						Log.d("FUT", "Received proper ACK on data " + Parser.getContentString(data));
						return true;
					} else if (Parser.getContentString(data).compareToIgnoreCase(Protocol.ACK) == 0) {
						Log.d("FUT", "Received ACK on data " + subject + " which is sufficient too");
						return true;
					}
					Log.e("FUT", "Incomming data " + Parser.getContentString(data));
					return false;
				}
				Log.e("FUT", "Incoming " + Parser.getMethod(data) + " instead of " + Protocol.METHOD.ACK);
				return false;
			} catch (IndexOutOfBoundsException e) {
				Log.e("FUT", "IndexOutOfBoundsException: " + e.getMessage());
				return false;
			} catch (ParserException e) {
				Log.e("FUT", e.getMessage());
				return false;
			}
		}
		Log.d("FUT", "Not connected");
		return false;
	}
	
	public boolean getAck() throws IOException {
		if (isConnected) {
			byte[] data = getData();
			try {
				if (Parser.getMethod(data) == Protocol.METHOD.ACK) {
					Log.d("FUT", "Credentials has been verified");
					return true;
				} else if (Parser.getMethod(data) == Protocol.METHOD.ERROR) {
					Log.e("FUT", "Error has come, probably due to wrong credentials");
					return false;
				}
				Log.e("FUT", "Incoming " + Parser.getMethod(data) + " instead of " + Protocol.METHOD.ACK);
				return false;
			} catch (IndexOutOfBoundsException e) {
				Log.e("FUT", "IndexOutOfBoundsException: " + e.getMessage());
				return false;
			} catch (ParserException e) {
				Log.e("FUT", e.getMessage());
				return false;
			}
		}
		Log.d("FUT", "Not connected");
		return false;
	}
	
	public String getIMEI() {
//		if (!isConnected) {
//			try {
//				connect();
//			} catch (UnknownHostException e) {
//				Log.e("FUT", "UnknownHostException: " + e.getMessage());	
//			} catch (IOException e) {
//				Log.e("FUT", "IOException: " + e.getMessage());
//			}
//		}
		
		String request = Protocol.GET + Protocol.SEPARATOR + Protocol.IMEI + Protocol.SEPARATOR + Protocol.SEPARATOR;
		try {
			socket.getOutputStream().write(request.getBytes());
			byte[] data = getData();
			if (Parser.getMethod(data) == Protocol.METHOD.ACK) {
				return new String(Parser.getContent(data)); 
			} else {
				Log.e("FUT", "Error: Expected " + Protocol.METHOD.ACK + " but got " + Parser.getMethod(data));
			}
		} catch (IOException e) {
			Log.e("FUT", "IOException: " + e.getMessage());
		} catch (ParserException e) {
			Log.e("FUT", e.getMessage());
		}
		return null;
	}
	
	/**
	 * Reads data from socket
	 * @param sock Socket
	 * @return String from date in socket
	 */
	private byte[] getData() throws IOException {
		Vector<byte[]> buffer = new Vector<byte[]>();
		byte[] message;
		int length = 0;
		int num;
		
		do {
			byte[] data = new byte[SIZE];
			num = socket.getInputStream().read(data);
			if (num < 0) {
				throw new IOException("Cannot read from socket");
			}
			buffer.add(data);
			length += num;
		} while (num == SIZE);

		message = new byte[length];

		copyDefault(message, buffer, num);
		
//		System.out.println("Zacatek");
//		System.out.println(new String(message));
//		//				System.out.println(new String(coder.decode(message)));
//		System.out.println("Konec");
		
		Log.d("FUT", "Incoming data: " + new String(coder.decode(message)));
		
		return coder.decode(message);
	}
	
	private void copyDefault(byte[] msg, Vector<byte[]> buf, int last) {
		for (int i=0; i < buf.size(); i++) {
			if (i == buf.size() - 1) {
				System.arraycopy(buf.elementAt(i), 0, msg, i*SIZE, last);
			}
			else {
				System.arraycopy(buf.elementAt(i), 0, msg, i*SIZE, SIZE);
			}
		}
	}
}
