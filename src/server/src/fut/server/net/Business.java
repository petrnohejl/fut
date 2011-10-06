package fut.server.net;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fut.common.DeviceObject;
import fut.common.FileObject;
import fut.common.GeodataObject;
import fut.common.Serializator;
import fut.server.common.Props;
import fut.server.db.DBLogic;
import fut.server.exception.ParserException;

public class Business {
	private static final String GENERALPWD = "FUT::Fut_Under_Table";
	private DBSaver_geodata dbgeo;
	private DBSaver_device dbdevice;
	private String uname;
	private Coder coder;
	private boolean first;
	private final Socket sock;
	private final Net net;
	private static final int SIZE = 512;
	private Map<String, Long> dataTypes;

	public Business(Net n, Socket s) {
		dbgeo = null;
		dbdevice = null;
		first = true;
		this.sock = s;
		this.net = n;
		dataTypes = DBLogic.getInstance().listDataTypes();
	}

	/**
	 * Runs server business logic
	 *
	 * @param propID
	 *            Properties ID
	 */
	// public void run(final String propID) {
	// int port =
	// Integer.parseInt(Props.getInstance().getProperty("futserver.port",
	// propID));
	//
	// try {
	// Net server = new Net(port);
	// server.run();
	// } catch (IOException ioe) {
	// System.err.println("IOException when creating server: " +
	// ioe.getMessage());
	// }
	// }

	private byte[] register(byte[] data) {
		Coder coder = new Coder(GENERALPWD);
		data = coder.decode(data);
		if (data == null) {
			System.err.println("Unable to decode data for registering");
			String response = new String(Protocol.ERROR + Protocol.SEPARATOR
					+ Protocol.SEPARATOR + Protocol.SEPARATOR
					+ Protocol.SEPARATOR);
			return coder.encode(response.getBytes());
		}
		String username = Parser.getUsername(data);
		String password = Parser.getContentString(data);

		System.out.println("Creating new user " + username);
		System.out.println("with password " + password);

		if (DBLogic.getInstance().createUser(username, password) == -1) {
			System.err.println("Cannot create user " + username);
			String response = new String(Protocol.ERROR + Protocol.SEPARATOR
					+ Protocol.SEPARATOR + "Unable to create new user"
					+ Protocol.SEPARATOR + Protocol.SEPARATOR);
			return coder.encode(response.getBytes());
		}

		String response = new String(Protocol.ACK + Protocol.SEPARATOR
				+ Protocol.ACK + Protocol.SEPARATOR + "user created"
				+ Protocol.SEPARATOR + Protocol.SEPARATOR);
		return coder.encode(response.getBytes());
	}

	private String ack() {
		return new String(Protocol.ACK + Protocol.SEPARATOR + Protocol.ACK
				+ Protocol.SEPARATOR + Protocol.SEPARATOR);
	}

	/**
	 *
	 * @param data
	 *            Data from client
	 * @param response
	 *            Prepared data for sending back to the client
	 * @return True if client is waiting for response, false otherwise
	 */
	public byte[] service(byte[] data) {
		byte[] resp = null;

		try {
			String id = Parser.getID(data);

			// Register
			if (id.isEmpty()) {
				System.out.println("id is empty, registering");
				return register(Parser.removeID(data));
			}

			System.out.println("id is " + id);

			if (first) {
				String password = DBLogic.getInstance().getUserPassword(id);
				if (password.equals(id)) {
					return null;
				}

				uname = new String(id);

				coder = new Coder(password);

				dbgeo = new DBSaver_geodata(uname);
				dbdevice = new DBSaver_device(uname);
				first = false;
			}

			if (!uname.equals(id)) {
				System.err.println("Bad ID, this should never happen");
				return null;
			}

			data = Parser.removeID(data);
			data = coder.decode(data);
			if (data == null) {
				return null;
			}

			System.out.println("Incoming data: " + new String(data));

			switch (Parser.getMethod(data)) {
			case PING:
				DBLogic.getInstance().loadUserProperties(uname);
				resp = ack().getBytes();
				break;
			case ACK:
				break;
			case GET:
				resp = get(data);
				if (resp == null || resp.length == 0) {
					resp = null;
				}
				break;
			case SAVE:
				resp = saveBytes(data);
				if (resp == null || resp.length == 0) {
					resp = null;
				}
				break;
			case ERROR:
				break;
			default:
				System.err.println("This will never happen");
			}
		} catch (IndexOutOfBoundsException e) {
			System.err.println("IndexOutOfBoundsException: " + e.getMessage());
		} catch (ParserException e) {
			System.err.println(e.getMessage());
		}

		System.out.println("Outgoing data: "
				+ ((resp == null) ? "null" : new String(resp)));
		if (resp == null) {
			resp = (new String(Protocol.ERROR + Protocol.SEPARATOR
					+ Protocol.SEPARATOR + "The response was null"
					+ Protocol.SEPARATOR + Protocol.SEPARATOR)).getBytes();
		}
		return coder.encode(resp);
	}

	private byte[] saveBytes(byte[] data) {
		String resp = save(data);
		if (resp == null) {
			return null;
		}
		return resp.getBytes();
	}

	private String saveFile(byte[] content, String type) {
		System.out.println("Saving " + type + ": " + new String(content));
		if (DBLogic.getInstance().createFile(type + ".csv", content, "",
				dataTypes.get(type), uname) == -1) {
			return new String(Protocol.ERROR + Protocol.SEPARATOR
					+ Protocol.ACCOUNTS + Protocol.SEPARATOR
					+ Protocol.SEPARATOR);
		}

		return new String(Protocol.ACK + Protocol.SEPARATOR + Protocol.ACCOUNTS
				+ Protocol.SEPARATOR + Protocol.ACK + Protocol.SEPARATOR);
	}


	private List<byte[]> makeList(String content) {
		byte[] cont = content.getBytes();
		int count = content.length() / SIZE;
		int rest = content.length() % SIZE;
		List<byte[]> list = new ArrayList<byte[]>(count);
		byte[] tmp;

		int i = 0;
		for (; i < count; i++) {
			tmp = new byte[SIZE];
			System.arraycopy(cont, i * SIZE, tmp, 0, SIZE);
			list.add(tmp);
		}

		tmp = new byte[rest];
		System.arraycopy(cont, i * SIZE, tmp, 0, rest);
		list.add(tmp);

		return list;
	}

	private byte[] makeByteFromList(List<byte[]> list) {
		int count = list.size() - 1;
		int rest = list.get(count).length;
		byte[] bytes = new byte[count * SIZE + rest];

		int i = 0;
		for (; i < count; i++) {
			System.arraycopy(list.get(i), 0, bytes, i * SIZE, SIZE);
		}
		System.arraycopy(list.get(i), 0, bytes, i * SIZE, rest);

		return bytes;
	}

	private void sendAck(String content, String type) {
		byte[] resp = (new String(Protocol.ACK + Protocol.SEPARATOR + type
				+ Protocol.SEPARATOR + content + Protocol.SEPARATOR))
				.getBytes();

		System.out.println("Sending back ack: " + new String(resp));
		resp = coder.encode(resp);
		try {
			sock.getOutputStream().write(resp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private byte[] getFile(int count) {
		byte[] tmp;
		List<byte[]> list = new ArrayList<byte[]>(count);
		try {
			for (int i = 0; i < count; i++) {
				System.out.println("Getting " + i + " of " + count );
				tmp = net.getData(sock);
				list.add(tmp);
			}
		} catch (IOException e) {
			System.err.println("IOException while reading from socket: "
					+ e.getMessage());
		}

		System.out.println("Got list of file");

		return coder.decode(makeByteFromList(list));
	}

	/**
	 * Saves proper data to the database
	 *
	 * @param data
	 *            Data stream from socket
	 * @return Reponse string
	 */
	private String save(byte[] data) {
		String content = Parser.getContentString(data);
		byte[] tmp;
		try {
			switch (Parser.getSubject(data)) {
			case IMEI:
				try {
					dbdevice.setIMEI(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.IMEI + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				System.out.println("Saving IMEI " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.IMEI + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case MCCMNC:
				try {
					dbdevice.setMSSMNC(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.MCCMNC + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				System.out.println("Saving MCCMNC " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.MCCMNC + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case SSE:
				try {
					dbdevice.setSSE(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.SSE + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				System.out.println("Saving SIM serial " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.SSE + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case LONG:
				try {
					dbgeo.setLNG(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.LONG + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}
				System.out.println("Saving GPS " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.LONG + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case LAT:
				try {
					dbgeo.setLAT(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.LAT + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				System.out.println("Saving latitude " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.LAT + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case IMSI:
				try {
					dbdevice.setIMSI(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.IMSI + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				System.out.println("Saving IMSI " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.IMSI + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case ACC:
				try {
					dbgeo.setACC(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.ACC + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				System.out.println("Saving accuracy " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.ACC + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case AZIM:
				try {
					dbgeo.setAZIM(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.AZIM + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				System.out.println("Saving azimuth " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.AZIM + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case ALT:
				try {
					dbgeo.setALT(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.ALT + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				System.out.println("Saving altitude " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.ALT + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case SPD:
				try {
					dbgeo.setSPD(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.SPD + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				System.out.println("Saving speed " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.SPD + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case SRC:
				try {
					dbgeo.setSRC(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.SRC + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				System.out.println("Saving source " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.SRC + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case TIME:
				try {
					dbgeo.setTIME(content);
				} catch (ParserException e) {
					System.err.println("Caught excpetion " + e.getMessage());
					return new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.TIME + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				System.out.println("Saving time " + content);
				return new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.TIME + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case STOLEN:
				if (DBLogic.getInstance().setDeviceStolen(uname,
						Boolean.parseBoolean(content))) {
					System.out.println("Saving stolen to " + content);
					return new String(Protocol.ACK + Protocol.SEPARATOR
							+ Protocol.STOLEN + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}

				return new String(Protocol.ERROR + Protocol.SEPARATOR
						+ Protocol.STOLEN + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case ALARM:
				if (DBLogic.getInstance().updateProperty("alarm", content,
						uname)) {
					return new String(Protocol.ACK + Protocol.SEPARATOR
							+ Protocol.ALARM + Protocol.SEPARATOR + content
							+ Protocol.SEPARATOR);
				}
				return new String(Protocol.ERROR + Protocol.SEPARATOR
						+ Protocol.ALARM + Protocol.SEPARATOR + content
						+ Protocol.SEPARATOR);

			case ACCOUNTS:
				sendAck(content, Protocol.ACCOUNTS);
				tmp = getFile(Integer.parseInt(content));
				return saveFile(tmp, "accounts");
				// if (DBLogic.getInstance().createFile("accounts.csv",
				// content.getBytes(), "", dataTypes.get("accounts"), uname) ==
				// -1) {
				// return new String(Protocol.ERROR + Protocol.SEPARATOR +
				// Protocol.ACCOUNTS + Protocol.SEPARATOR + Protocol.SEPARATOR);
				// }
				//
				// return new String(Protocol.ACK + Protocol.SEPARATOR +
				// Protocol.ACCOUNTS + Protocol.SEPARATOR + Protocol.ACK +
				// Protocol.SEPARATOR);

			case CONTACTS:
				sendAck(content, Protocol.CONTACTS);
				tmp = getFile(Integer.parseInt(content));
				return saveFile(tmp, "contacts");
				// if (DBLogic.getInstance().createFile("contacts.csv",
				// content.getBytes(), "", dataTypes.get("contacts"), uname) ==
				// -1) {
				// return new String(Protocol.ERROR + Protocol.SEPARATOR +
				// Protocol.CONTACTS + Protocol.SEPARATOR + Protocol.SEPARATOR);
				// }
				// return new String(Protocol.ACK + Protocol.SEPARATOR +
				// Protocol.CONTACTS + Protocol.SEPARATOR + Protocol.ACK +
				// Protocol.SEPARATOR);

			case HISTORY:
				sendAck(content, Protocol.HISTORY);
				tmp = getFile(Integer.parseInt(content));
				return saveFile(tmp, "history");
				// if (DBLogic.getInstance().createFile("history.csv",
				// content.getBytes(), "", dataTypes.get("history"), uname) ==
				// -1) {
				// return new String(Protocol.ERROR + Protocol.SEPARATOR +
				// Protocol.CONTACTS + Protocol.SEPARATOR + Protocol.SEPARATOR);
				// }
				// return new String(Protocol.ACK + Protocol.SEPARATOR +
				// Protocol.HISTORY + Protocol.SEPARATOR + Protocol.ACK +
				// Protocol.SEPARATOR);

			default:
				System.err.println("This will never happen");
			}
		} catch (IndexOutOfBoundsException e) {
			System.err.println("IndexOutOfBoundsException: " + e.getMessage());
		} catch (ParserException e) {
			System.err.println(e.getMessage());
		}

		return null;
	}

	/**
	 * Gets data from the database depending on data stream
	 *
	 * @param data
	 *            Data stream from socket
	 * @return Data from database
	 */
	private byte[] get(byte[] data) {
		String subject = null;
		byte[] retval = null;
		try {
			switch (Parser.getSubject(data)) {

			case IMEI:
				subject = Long.toString(DBLogic.getInstance().getDevices(uname)
						.get(0).getImei());
				System.out.println("Got IMEI " + subject);
				return (new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.IMEI + Protocol.SEPARATOR + subject
						+ Protocol.SEPARATOR)).getBytes();

			case IMSI:
				subject = Long.toString(DBLogic.getInstance().getDevices(uname)
						.get(0).getImsi());
				System.out.println("Got IMSI " + subject);
				return (new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.IMSI + Protocol.SEPARATOR + subject
						+ Protocol.SEPARATOR)).getBytes();

			case ALARM:
				subject = Props.getInstance().getProperty("alarm", uname);
				System.out.println("Got alarm " + subject);
				return (new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.ALARM + Protocol.SEPARATOR + subject
						+ Protocol.SEPARATOR)).getBytes();

				// case ACC: subject = Long.toString(DBLogic.getInstance().get
				// System.out.println("Got IMEI " + subject);
				// return new String(Protocol.ACK + Protocol.SEPARATOR +
				// Protocol.IMEI + Protocol.SEPARATOR + subject +
				// Protocol.SEPARATOR);
				// case ALT:
				// case AZIM:
				// case LAT:
				// case LONG:
				// case SPD:
				// case SRC:
				// case TIME:
				// break;
			case GEODATA:
				System.out.println("Getting " + Parser.getContentString(data)
						+ " geo-locations");
				List<GeodataObject> geodata = (List<GeodataObject>) DBLogic
						.getInstance()
						.getGeodata(uname,
								Integer.parseInt(Parser.getContentString(data)));
				try {
					subject = new String(Serializator.save64(geodata));
					System.err.println(subject);
				} catch (IOException e) {
					System.err
							.println("Error while serializng list of geodata");
					return (new String(Protocol.ERROR + Protocol.SEPARATOR
							+ Protocol.GEODATA + Protocol.SEPARATOR
							+ Protocol.SEPARATOR)).getBytes();
				}

				System.out.println("Sending back serialized list of geodata");
				return (new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.GEODATA + Protocol.SEPARATOR + subject
						+ Protocol.SEPARATOR)).getBytes();

			case STOLEN:
				subject = Boolean.toString(DBLogic.getInstance()
						.isDeviceStolen(uname));
				return (new String(Protocol.ACK + Protocol.SEPARATOR
						+ Protocol.STOLEN + Protocol.SEPARATOR + subject
						+ Protocol.SEPARATOR)).getBytes();

			case DEVICE:
				List<DeviceObject> dobjects = (List<DeviceObject>) DBLogic
						.getInstance().getDevices(uname);
				try {
					subject = new String(Serializator.save64(dobjects));

					return (new String(Protocol.ACK + Protocol.SEPARATOR
							+ Protocol.DEVICE + Protocol.SEPARATOR + subject
							+ Protocol.SEPARATOR)).getBytes();
				} catch (IOException e) {
					System.err
							.println("IOException while serializing Devices: "
									+ e.getMessage());
					return null;
				}

			case ACCOUNTS: {
				List<FileObject> files = DBLogic.getInstance().getFileData(
						dataTypes.get("accounts"), uname,
						Integer.parseInt(Parser.getContentString(data)));
				try {
					subject = new String(Serializator.save64(files));

					return (new String(Protocol.ACK + Protocol.SEPARATOR
							+ Protocol.ACCOUNTS + Protocol.SEPARATOR + subject
							+ Protocol.SEPARATOR)).getBytes();
				} catch (IOException e) {
					System.err
							.println("IOException while serializing Accounts: "
									+ e.getMessage());
					return null;
				}
			}

			case HISTORY: {
				List<FileObject> files = DBLogic.getInstance().getFileData(
						dataTypes.get("history"), uname,
						Integer.parseInt(Parser.getContentString(data)));
				try {
					subject = new String(Serializator.save64(files));

					return (new String(Protocol.ACK + Protocol.SEPARATOR
							+ Protocol.HISTORY + Protocol.SEPARATOR + subject
							+ Protocol.SEPARATOR)).getBytes();
				} catch (IOException e) {
					System.err
							.println("IOException while serializing Accounts: "
									+ e.getMessage());
					return null;
				}
			}

			case CONTACTS: {
				List<FileObject> files = DBLogic.getInstance().getFileData(
						dataTypes.get("contacts"), uname,
						Integer.parseInt(Parser.getContentString(data)));
				try {
					subject = new String(Serializator.save64(files));

					return (new String(Protocol.ACK + Protocol.SEPARATOR
							+ Protocol.CONTACTS + Protocol.SEPARATOR + subject
							+ Protocol.SEPARATOR)).getBytes();
				} catch (IOException e) {
					System.err
							.println("IOException while serializing Accounts: "
									+ e.getMessage());
					return null;
				}
			}

			default:
				System.err
						.println("This will never happen or unimplemented subject "
								+ Parser.getSubject(data));
			}
		} catch (IndexOutOfBoundsException e) {
			System.err.println("IndexOutOfBoundsException: " + e.getMessage());
			return null;
		} catch (ParserException e) {
			System.err.println(e.getMessage());
			return null;
		}

		return null;
	}
}
