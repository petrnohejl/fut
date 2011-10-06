package fut.server.net;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import fut.server.net.Protocol;

/**
 *
 * @author jlibosva
 *
 */
public class Net {
	private ServerSocket listen;
	private final int SIZE = 1024;
	private final Net instance;

	/**
	 *
	 * @param portd
	 */
	public Net(int port) throws IOException {
		this.listen = new ServerSocket(port);
		System.out.println("Server is running on port " + port);
		instance = this;
	}

	/**
	 * The main method that runs daemon. Accepts new connections and gives the socket to the service.
	 */
	public void run() {
		Thread t = new Thread() {

			public void run() {
				if (listen == null) {
					System.err.println("Error: Listen is null");
					return;
				}
				System.out.println("Entering the main loop");
				while (true) {
					try {
						final Socket sock = listen.accept();
						service(sock);
					} catch (IOException e) {
						System.err.println("IOException while accepting connection: " + e.getMessage());
					} catch (Exception e) {
						System.err.println("Exception while accepting connection: " + e.getMessage());
					}
				}
			}
		};

		t.setDaemon(true);
		t.start();
	}

	/**
	 * Reads data from socket
	 * @param sock Socket
	 * @return String from date in socket
	 */
	public byte[] getData(final Socket sock) throws IOException {
		Vector<byte[]> buffer = new Vector<byte[]>();
		byte[] message;
		int length = 0;
		int num;

		do {
			byte[] data = new byte[SIZE];
			num = sock.getInputStream().read(data);
			if (num < 0) {
				throw new IOException("Cannot read from socket");
			}
			buffer.add(data);
			length += num;
		} while (num == SIZE);

		message = new byte[length];

		copyDefault(message, buffer, num);

		return message;
	}

	/**
	 * Reads data from socket
	 * @param sock Socket
	 * @return String from date in socket
	 */
	private String getDataString(final Socket sock) throws IOException {
		return new String(getData(sock));
	}

	/**
	 * Service method, that communicates with the client.
	 * @param sock The socket for communication with the client.
	 * @return 0 on success
	 */
	private int service(final Socket sock) {
		Thread t = new Thread() {
			private boolean connected = true;

			public void run() {
				byte[] data;
				Business business = new Business(instance, sock);

				try {
					System.out.println("Connection has come");
					System.out.println("Got address: " + sock.getRemoteSocketAddress().toString());
					while(connected) {
						data = getData(sock);
						byte[] response = business.service(data);
						if (response != null) {
							System.out.println("Responding: " + new String(response));
							System.out.println("Length: " + response.length);
							System.out.println("--------------------------------------");
							sock.getOutputStream().write(response, 0, response.length);
						} else {
							System.err.println("Error getting response");
							connected = false;
						}
					}
				} catch (IOException e) {
					System.err.println("IOException in service: " + e.getMessage());
				} catch (IndexOutOfBoundsException e) {
					System.err.println("IndexOutOfBoundsException in service: " + e.getMessage());
				} catch (Exception e) {
//					System.err.println("Exception in Net.service: " + e.getMessage());
				    	e.printStackTrace();
					System.out.println("Client has disconnected");
				} finally {
					try {
						sock.close();
						connected = false;
					} catch (IOException e) {
						System.err.println("Cannot close socket");
					}
				}
			}
		};

		t.setDaemon(true);
		t.start();
		return 0;
	}

	private void copy1(byte[] msg, Vector<byte[]> buf) {
		for (int i=0; i < buf.size()-1; i++) {
			if (i == buf.size() - 2) {
				System.arraycopy(buf.elementAt(i), 0, msg, i*SIZE, SIZE-1);
			}
			else {
				System.arraycopy(buf.elementAt(i), 0, msg, i*SIZE, SIZE);
			}
		}
	}

	private void copy2(byte[] msg, Vector<byte[]> buf) {
		for (int i=0; i < buf.size()-1; i++) {
			System.arraycopy(buf.elementAt(i), 0, msg, i*SIZE, SIZE);
		}
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
