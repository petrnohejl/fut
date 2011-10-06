package fut.client.net;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import fut.client.common.InvalidUserException;
import fut.common.DeviceObject;
import fut.common.FileObject;
import fut.common.GeodataObject;
import fut.common.Serializator;


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

	private final int SIZE = 1024;

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
	}

	public boolean isConnected() {
		return this.isConnected;
	}

	private boolean registerUser(String id, String pwd) {
		byte[] stream = (Protocol.REG + Protocol.SEPARATOR + id + Protocol.SEPARATOR + pwd + Protocol.SEPARATOR).getBytes();
		System.out.println("register stream(without id$) length: " + stream.length);
		byte[] coded = coder.encode(stream);
		byte[] header = Character.toString(Protocol.SEPARATOR).getBytes();
		byte[] data = new byte[header.length + coded.length];

		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(coded, 0, data, header.length, coded.length);

		System.out.println("register data length: " + data.length);

		System.out.println("Sending register content: " + new String(header) + new String(stream));
		System.out.println("Sending register encoded: " + new String(data));

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

	public void register(String id, String pwd) throws InvalidUserException, IOException {
		try {
			if (!isConnected) {
				socket = new Socket(server, port);
				isConnected = true;
				if (registerUser(id, pwd)) {
					disconnect();
					throw new InvalidUserException("Bad username or password");
				}
			}
		}
		catch (UnknownHostException e) {
			System.err.println("UnknownHostExcpetion in connect: " + e.getMessage());
			throw e;
		}
		catch (IOException e) {
			System.err.println(Net.class.getName() + " - connect IOException: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Creates new socket for communication
	 * @return True on error, false otherwise
	 */
	public void connect(String id) throws UnknownHostException, IOException, InvalidUserException {
		try {
			if (!isConnected) {
				socket = new Socket(server, port);
				isConnected = true;
				if (ping(id)) {
					disconnect();
					throw new InvalidUserException("Bad username or password");
				}
			}
		}
		catch (UnknownHostException e) {
		    	isConnected = false;
			System.err.println("UnknownHostExcpetion in connect: " + e.getMessage());
			throw e;
		}
		catch (IOException e) {
		    	isConnected = false;
			System.err.println(Net.class.getName() + " - connect IOException: " + e.getMessage());
			throw e;
		}
	}

	/**
	 *
	 * @param data
	 * @throws IOException
	 */
	private void send(byte[] data) throws IOException {
		if (!isConnected) {
			System.err.println(Net.class.getName() + " - send Sending data, but not connected");
			return;
		}
		System.out.println(Net.class.getName() + " - send Message: " + new String(data));
		System.out.println("Send Message length: " + data.length);
		socket.getOutputStream().write(data);
		System.out.println("Message sent");
	}

	/**
	 *
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		if (!isConnected) {
			System.err.println(Net.class.getName() + " - disconnect: Disconnecting but not connected");
			return;
		}
		System.err.println(Net.class.getName() + " Closing socket");
		if (socket != null) socket.close();
		isConnected = false;
		System.err.println(Net.class.getName() + " Socket closed");
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

	/**
	 *
	 * @param id
	 * @param accountsFile
	 * @return
	 */
	public int sendAccounts(String id, String accountsFile) {
		return saveGeneral(id, Protocol.ACCOUNTS, accountsFile);
	}

	/**
	 *
	 * @param id
	 * @param contactsFile
	 * @return
	 */
	public int sendContacts(String id, String contactsFile) {
		return saveGeneral(id, Protocol.CONTACTS, contactsFile);
	}

	/**
	 *
	 * @param id
	 * @param historyFile
	 * @return
	 */
	public int sendHistory(String id, String historyFile) {
		return saveGeneral(id, Protocol.HISTORY, historyFile);
	}

	public int sendAlarm(String id, boolean val) {
		return saveGeneral(id, Protocol.ALARM, Boolean.toString(val));
	}

	public int sendStolen(String id, boolean val) {
		return saveGeneral(id, Protocol.STOLEN, Boolean.toString(val));
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
		System.err.println("saveGeneral stream(without id$) length: " + stream.length);
		byte[] coded = coder.encode(stream);
		byte[] header = (id + Protocol.SEPARATOR).getBytes();
		byte[] data = new byte[header.length + coded.length];

		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(coded, 0, data, header.length, coded.length);

		System.err.println("saveGeneral data length: " + data.length);

		System.err.println("Sending saveGeneral content: " + id + Protocol.SEPARATOR + new String(stream));
		System.err.println("Sending saveGeneral encoded: " + new String(data));

		try {
			send(data);
			if (getAck(content)) {
				return 0;
			} else {
				System.err.println(Net.class.getName() + " - send Sending has failed.");
				return 1;
			}
		} catch (IOException e) {
			System.err.println(Net.class.getName() + " - send IOException: " + e.getMessage());
			e.printStackTrace();
			return 1;
		}
	}

	private boolean ping(String id) {
		byte[] stream = (Protocol.PING + Protocol.SEPARATOR + Protocol.SEPARATOR + Protocol.SEPARATOR).getBytes();
		System.out.println("ping stream(without id$) length: " + stream.length);
		byte[] coded = coder.encode(stream);
		byte[] header = (id + Protocol.SEPARATOR).getBytes();
		byte[] data = new byte[header.length + coded.length];

		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(coded, 0, data, header.length, coded.length);

		System.out.println("ping data length: " + data.length);

		System.out.println("Sending ping content: " + id + Protocol.SEPARATOR + new String(stream));
		System.out.println("Sending ping encoded: " + new String(data));

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
			//e.printStackTrace();
			return true;
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
			System.err.println("getStolen Unknown stolen status " + stolen);
		}

		return false;
	}

	public boolean getAlarm(String id) {
		String stolen = getGeneral(id, Protocol.ALARM);

		if (stolen.compareToIgnoreCase("true") == 0) {
			return true;
		} else if (stolen.compareToIgnoreCase("false") == 0) {
			return false;
		} else {
			System.err.println("getAlarm Unknown stolen status " + stolen);
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public List<DeviceObject> getDevice(String id) {
		String deserializedDeviceString = getGeneral(id, Protocol.DEVICE);
		try {
			return (List<DeviceObject>) Serializator.load64(deserializedDeviceString);
		} catch (IOException e) {
			System.err.println("Error while deserializing device list");
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<GeodataObject> getGeodata(String id, int count) {
		byte[] deser = getGeneralBytes(id, Protocol.GEODATA, Integer.toString(count));
		try {
			System.out.println("Got geodata");
			return (List<GeodataObject>)Serializator.load64(new String(deser));
		} catch (IOException e) {
			System.err.println("Error while deserializing geodata list, probably no, or corrupted data");
		}
		return null;
	}

	public List<FileObject> getContacts(String id, int limit ) {
	    return getFile(id, Protocol.CONTACTS, Integer.toString(limit));
	}

	public List<FileObject> getAccounts(String id, int limit ) {
	    return getFile(id, Protocol.ACCOUNTS, Integer.toString(limit));
	}

	public List<FileObject> getHistory(String id, int limit ) {
	    return getFile(id, Protocol.HISTORY, Integer.toString(limit));
	}

	@SuppressWarnings("unchecked")
	private List<FileObject> getFile(String id, String fileSubject, String limit ) {
		byte[] serializedFiles = getGeneralBytes(id, fileSubject, limit);
		try {
			return (List<FileObject>) Serializator.load64(new String(serializedFiles));
		} catch (IOException e) {
			System.err.println("Error while deserializing a list of file objects");
			return null;
		}

	}

	/**
	 * General method for getting data
	 * @param id User's ID
	 * @param subject Subject user wants to get
	 * @return Response on success, null otherwise
	 */
	private byte[] getGeneralBytes(String id, String subject, String content) {
		byte[] stream = (Protocol.GET + Protocol.SEPARATOR + subject + Protocol.SEPARATOR + content + Protocol.SEPARATOR).getBytes();
		System.out.println("getGeneral stream(without id$) length: " + stream.length);
		byte[] coded = coder.encode(stream);
		byte[] header = (id + Protocol.SEPARATOR).getBytes();
		byte[] data = new byte[header.length + coded.length];

		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(coded, 0, data, header.length, coded.length);

		System.out.println("getGeneral data length: " + data.length);

		System.out.println("Sending getGeneral content: " + id + Protocol.SEPARATOR + new String(stream));
		System.out.println("Sending getGeneral encoded: " + data);

		try {
			send(data);
			System.out.println("Waiting for data");
			return getResponseBytes(subject);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * General method for getting data
	 * @param id User's ID
	 * @param subject Subject user wants to get
	 * @return Response on success, null otherwise
	 */
	private byte[] getGeneralBytes(String id, String subject) {
		byte[] stream = (Protocol.GET + Protocol.SEPARATOR + subject + Protocol.SEPARATOR +  Protocol.SEPARATOR).getBytes();
		System.out.println("getGeneral stream(without id$) length: " + stream.length);
		byte[] coded = coder.encode(stream);
		byte[] header = (id + Protocol.SEPARATOR).getBytes();
		byte[] data = new byte[header.length + coded.length];

		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(coded, 0, data, header.length, coded.length);

		System.out.println("getGeneral data length: " + data.length);

		System.out.println("Sending getGeneral content: " + id + Protocol.SEPARATOR + new String(stream));
		System.out.println("Sending getGeneral encoded: " + data);

		try {
			send(data);

			return getResponseBytes(subject);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * General method for getting data
	 * @param id User's ID
	 * @param subject Subject user wants to get
	 * @return Response on success, null otherwise
	 */
	private String getGeneral(String id, String subject) {
		byte[] stream = (Protocol.GET + Protocol.SEPARATOR + subject + Protocol.SEPARATOR +  Protocol.SEPARATOR).getBytes();
		System.out.println("getGeneral stream(without id$) length: " + stream.length);
		byte[] coded = coder.encode(stream);
		byte[] header = (id + Protocol.SEPARATOR).getBytes();
		byte[] data = new byte[header.length + coded.length];

		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(coded, 0, data, header.length, coded.length);

		System.out.println("getGeneral data length: " + data.length);

		System.out.println("Sending getGeneral content: " + id + Protocol.SEPARATOR + new String(stream));
		System.out.println("Sending getGeneral encoded: " + new String(data));

		try {
			send(data);
			System.out.println("Getting response");
			return getResponse(subject);

		} catch (IOException e) {
			System.err.println(Net.class.getName() + " - sendIMEI IOException: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private byte[] getResponseBytes(String subject) {
		if (isConnected) {
			try {
				System.out.println("Getting data");
				byte[] data = getData();
				System.out.println("Data read successfully");
				switch(Parser.getMethod(data)) {
				case ERROR: System.err.println("Error came from server!");
					return null;
				case ACK: return Parser.getContent(data);
					default:
				}
			} catch (IOException e) {
				System.err.println("getResponse IOException: " + e.getMessage());
			} catch (IndexOutOfBoundsException e) {
				System.err.println("getResponse IndexOutOfBoundsException" + e.getMessage());
			} catch (ParserException e) {
				System.err.println("getResponse" + e.getMessage());
			}
		} else {
			System.err.println("NOT CONNECTED!!!");
		}

		return new byte[0];
		//return null;
	}

	private String getResponse(String subject) {
		if (isConnected) {
			try {
				byte[] data = getData();
				switch(Parser.getMethod(data)) {
				case ERROR: System.err.println("Error came from server!");
					return null;
				case ACK: return Parser.getContentString(data);
					default:
				}
			} catch (IOException e) {
				System.err.println("getResponse IOException: " + e.getMessage());
			} catch (IndexOutOfBoundsException e) {
				System.err.println("getResponse IndexOutOfBoundsException" + e.getMessage());
			} catch (ParserException e) {
				System.err.println("getResponse" + e.getMessage());
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
						System.err.println("getAck Received proper ACK on data " + Parser.getContentString(data));
						return true;
					} else if (Parser.getContentString(data).compareToIgnoreCase(Protocol.ACK) == 0) {
						System.err.println("getAck Received ACK on data " + subject + " which is sufficient too");
						return true;
					}
					System.err.println("getAck Incomming data " + Parser.getContentString(data));
					return false;
				}
				System.err.println("getAck Incoming " + Parser.getMethod(data) + " instead of " + Protocol.METHOD.ACK);
				return false;
			} catch (IndexOutOfBoundsException e) {
				System.err.println("getAck IndexOutOfBoundsException: " + e.getMessage());
				return false;
			} catch (ParserException e) {
				System.err.println("getAck" + e.getMessage());
				return false;
			}
		}
		System.err.println("getAck Not connected");
		return false;
	}

	public boolean getAck() throws IOException {
		if (isConnected) {
			byte[] data = getData();
			try {
				if (Parser.getMethod(data) == Protocol.METHOD.ACK) {
					return true;
				} else if (Parser.getMethod(data) == Protocol.METHOD.ERROR) {
					System.err.println("Response for ping is error, probably wrong credentials");
					return false;
				}
				System.err.println("getAck Incoming " + Parser.getMethod(data) + " instead of " + Protocol.METHOD.ACK);
				return false;
			} catch (IndexOutOfBoundsException e) {
				System.err.println("getAck IndexOutOfBoundsException: " + e.getMessage());
				return false;
			} catch (ParserException e) {
				System.err.println("getAck" + e.getMessage());
				return false;
			}
		}
		System.err.println("getAck Not connected");
		return false;
	}

	public String getIMEI() {
//		if (!isConnected) {
//			try {
//				connect();
//			} catch (UnknownHostException e) {
//				System.err.println(Net.class.getName() + " - getIMEI UnknownHostException: " + e.getMessage());
//			} catch (IOException e) {
//				System.err.println(Net.class.getName() + " - getIMEI IOException: " + e.getMessage());
//			}
//		}

		String request = Protocol.GET + Protocol.SEPARATOR + Protocol.IMEI + Protocol.SEPARATOR + Protocol.SEPARATOR;
		try {
			socket.getOutputStream().write(request.getBytes());
			byte[] data = getData();
			if (Parser.getMethod(data) == Protocol.METHOD.ACK) {
				return new String(Parser.getContent(data));
			} else {
				System.err.println(Net.class.getName() + " - getIMEI Error: Expected " + Protocol.METHOD.ACK + " but got " + Parser.getMethod(data));
			}
		} catch (IOException e) {
			System.err.println(Net.class.getName() + " - getIMEI IOException: " + e.getMessage());
		} catch (ParserException e) {
			System.err.println(Net.class.getName() + " - getIMEI" + e.getMessage());
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
				System.err.println("Error while reading from socket");
				throw new IOException("Cannot read from socket");
			}
			buffer.add(data);
			length += num;
			System.out.println("Another reading loop");
		} while (num == SIZE);

		System.out.println("Length of message: " + length);
		message = new byte[length];

		copyDefault(message, buffer, num);

		System.out.println("Zacatek");
		System.out.println(new String(message));
		//				System.out.println(new String(coder.decode(message)));
		System.out.println("Konec");

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
