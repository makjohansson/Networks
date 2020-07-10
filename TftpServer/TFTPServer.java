package mj223vn_assign3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import mj223vn_assign3.TFTPErrors.Errors;

/**
 * TFTP server, only octet mode support
 * 
 * @author Marcus Johansson
 *
 */
public class TFTPServer {
	public static final int TFTPPORT = 4970;
	public static final int BUFSIZE = 516;
	public static final String READDIR = "/Users/marcus/Lnu/ComputerNetwork/1DV701/write/"; // custom address at your PC
	public static final String WRITEDIR = "/Users/marcus/Lnu/ComputerNetwork/1DV701/write/"; // custom address at your
																								// PC
	public static final int TIMEOUT = 2000;
	public static final int RESEND_LIMIT = 10;

	// OP codes
	public static final int OP_RRQ = 1;
	public static final int OP_WRQ = 2;
	public static final int OP_DAT = 3;
	public static final int OP_ACK = 4;
	public static final int OP_ERR = 5;

	TFTPErrors errorHandler;
	private boolean validMode = true;
	private final File DIR_SIZE = new File(WRITEDIR);
	short opcode = 0;
	short block = 0;
	int sentSize = 0;
	boolean wasTimeOut;

	public static void main(String[] args) {
		if (args.length > 0) {
			System.err.printf("usage: java %s\n", TFTPServer.class.getCanonicalName());
			System.exit(1);
		}
		// Starting the server
		try {
			TFTPServer server = new TFTPServer();
			server.start();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void start() throws SocketException {
		byte[] buf = new byte[BUFSIZE];

		// Create socket
		DatagramSocket socket = new DatagramSocket(null);

		// Create local bind point
		SocketAddress localBindPoint = new InetSocketAddress(TFTPPORT);
		socket.bind(localBindPoint);

		System.out.printf("Listening at port %d for new requests\n", TFTPPORT);

		// Loop to handle client requests
		while (true) {

			final InetSocketAddress clientAddress = receiveFrom(socket, buf);

			// If clientAddress is null, an error occurred in receiveFrom()
			if (clientAddress == null)
				continue;
			final StringBuffer requestedFile = new StringBuffer();
			final int reqtype = ParseRQ(buf, requestedFile);

			new Thread() {
				public void run() {
					try {
						DatagramSocket sendSocket = new DatagramSocket(0);

						// Create error handler
						errorHandler = new TFTPErrors(sendSocket, validMode);

						// Connect to client
						sendSocket.connect(clientAddress);

						if (validMode) {
							System.out.printf("%s request from %s using port %d\n",
									(reqtype == OP_RRQ) ? "Read" : "Write", clientAddress.getHostName(),
									clientAddress.getPort());

							// Read request
							if (reqtype == OP_RRQ) {
								requestedFile.insert(0, READDIR);
								HandleRQ(sendSocket, requestedFile.toString(), OP_RRQ);

							}
							// Write request
							else if (reqtype == OP_WRQ) {

								requestedFile.insert(0, WRITEDIR);
								HandleRQ(sendSocket, requestedFile.toString(), OP_WRQ);
							}
							// Send TFTP error code 4
							else {
								errorHandler.sendError(Errors.err_4);
								System.err.println("Illegal TFTP operation");
							}
						} else
							errorHandler.sendError(Errors.err_0);
						sendSocket.close();
					} catch (SocketException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	/**
	 * Reads the first block of data, i.e., the request for an action (read or
	 * write).
	 * 
	 * @param socket (socket to read from)
	 * @param buf    (where to store the read data)
	 * @return socketAddress (the socket address of the client)
	 */
	private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) {
		// Create datagramPacket
		DatagramPacket recivePacket = new DatagramPacket(buf, buf.length);
		// Receive packet
		try {
			socket.receive(recivePacket);
		} catch (IOException ioe) {
			System.err.println("Error on socket reciving packet " + ioe);
		}
		// Get client address and port from the packet

		return new InetSocketAddress(recivePacket.getAddress(), recivePacket.getPort());
	}

	/**
	 * Parses the request in buf to retrieve the type of request and requestedFile
	 * 
	 * @param buf           (received request)
	 * @param requestedFile (name of file to read/write)
	 * @return opcode (request type: RRQ or WRQ)
	 */
	private int ParseRQ(byte[] buf, StringBuffer requestedFile) {
		// See "TFTP Formats" in TFTP specification for the RRQ/WRQ request contents
		ByteBuffer wrap = ByteBuffer.wrap(buf);
		requestedFile.append(new String(buf, 2, buf.length - 2));
		String mode = requestedFile.toString().split("\0")[1];

		if (!mode.equalsIgnoreCase("octet")) {
			System.err.println("invalid mode");
			validMode = false;
		}
		return wrap.getShort();
	}

	/**
	 * Handles RRQ and WRQ requests
	 * 
	 * @param sendSocket    (socket used to send/receive packets)
	 * @param requestedFile (name of file to read/write)
	 * @param opcode        (RRQ or WRQ)
	 */
	private void HandleRQ(DatagramSocket sendSocket, String requestedFile, int opcode) {
		if (opcode == OP_RRQ) {
			int blockNumber = 0;
			// See "TFTP Formats" in TFTP specification for the DATA and ACK packet contents
			@SuppressWarnings("unused")
			boolean result = send_DATA_receive_ACK(sendSocket, requestedFile, ++blockNumber);
		} else if (opcode == OP_WRQ) {
			int blockNumber = 0;
			@SuppressWarnings("unused")
			boolean result = receive_DATA_send_ACK(sendSocket, requestedFile, blockNumber);
		} else {
			System.err.println("Invalid request. Sending an error packet.");
			// See "TFTP Formats" in TFTP specification for the ERROR packet contents
			errorHandler.sendError(Errors.err_4);
			return;
		}
	}

	/**
	 * Request from client is a RRQ<br>
	 * Send requested file to client in packages in sizes of 516 (2 bytes OPCode(3),
	 * 2 bytes block number and 512 bytes from file)<br>
	 * If send or receive fails, the package will be resent as many times as
	 * RESEND_LIMIT is set too or till the socket will timeout
	 * 
	 * @param sendSocket    socket to send the package
	 * @param requestedFile filename from RRQ
	 * @param blockNumber   number of the package block number
	 * @return
	 */
	private boolean send_DATA_receive_ACK(DatagramSocket sendSocket, String requestedFile, int blockNumber) {
		String path = requestedFile.split("\0")[0];
		File file = new File(path);

		if (!file.exists()) {
			errorHandler.sendError(Errors.err_1);
			System.out.println("File not found");
			file.delete();
		} else {
			try {

				// Read file to a byte array
				byte[] dataPacket = Files.readAllBytes(file.toPath());

				int readIndex = 0;
				int maxSize = 512;
				boolean lastPacket = false;
				short opcode = 0;
				short block = 0;
				int sentSize = 0;
				int resends = 0;

				do {
					Packet packet = new Packet(sendSocket);
					wasTimeOut = false;
					// True when time to send last packet
					if (lastPacket == true) {
						maxSize = dataPacket.length;
					}

					// True if first packet is smaller than 512 bytes
					if (dataPacket.length < maxSize && lastPacket == false)
						maxSize = dataPacket.length;

					byte[] readBytes = Arrays.copyOfRange(dataPacket, readIndex, maxSize);

					do { // do-while to resend package that failed
						try {
							wasTimeOut = false;

							// Socket will throw a time out exception and prevent the program to end up into
							// an endless retransmission loop
							sendSocket.setSoTimeout(TIMEOUT);

							// send data packet
							packet.sendDataPacket(blockNumber, readBytes);

							// receive ACK for data packet
							packet.receiveACKPacket();

							// Check if received package arrived from same address sendDataPacket was sent
							// to
							if (packet.getAddress() != sendSocket.getInetAddress()) {
								errorHandler.sendError(Errors.err_5);
								System.err.println("Unknown transfer ID");
								break;
							}

							// Get OP and block number from ACK
							opcode = packet.getOpcode();
							block = packet.getBlock();

							if (opcode == OP_ERR) {
								System.out.println("Error message from client, server exit the connectinon");
								break;
							}

						} catch (SocketTimeoutException ste) {
							resends++;
							wasTimeOut = true;
						}

					} while (OP_ACK != opcode && block != blockNumber && resends < RESEND_LIMIT);

					if (opcode == OP_ERR) {
						break;
					}

					// Logic for reading file and send packages in right sizes
					// Will be ignored if an timeout was triggered
					if (wasTimeOut == false) {
						// Will be printed when a successful transmission is done
						sentSize += readBytes.length;

						blockNumber++;
						if (lastPacket == true) {
							block++;
						}
						if (maxSize + 512 >= dataPacket.length) {
							readIndex = maxSize;
							lastPacket = true;
						} else {
							readIndex = maxSize;
							maxSize = maxSize + 512;
						}
					}

				} while (maxSize < dataPacket.length && resends < RESEND_LIMIT
						|| block != blockNumber && resends < RESEND_LIMIT);

				System.out.println("Read request was successful\nSent " + sentSize + "bytes");

			} catch (FileNotFoundException fnf) {
				System.err.println("File in read request not found " + fnf);

			} catch (IOException ioe) {
				System.err.println("I/O error when reading file to byte array " + ioe);
				errorHandler.sendError(Errors.err_2);
			}
		}
		return true;

	}

	/**
	 * Request from TFTP client is WRQ<br>
	 * Receive package from client of size 516 until package data length is less
	 * than 512,then send last ACK then file transmission is completed. If not
	 * receiving packages in correct order or transmission is taking to long,
	 * transmission will be terminated
	 * 
	 * @param sendSocket    socket to use for send and receive
	 * @param requestedFile file name of file saved on disk
	 * @param blockNumber   number of the package block number
	 * @return
	 */
	private boolean receive_DATA_send_ACK(DatagramSocket sendSocket, String requestedFile, int blockNumber) {
		int receivedSize = 0;
		byte[] data;
		short opcode;
		short block;

		Packet packet = new Packet(sendSocket);

		String path = requestedFile.split("\0")[0];
		File fileToWrite = new File(path);
		try {
			// Socket will throw a time out exception and prevent the program to end up into
			// an endless retransmission loop
			sendSocket.setSoTimeout(TIMEOUT);

			// If file exists send TFTP error code 6
			if (fileToWrite.exists()) {
				// Sleep to avoid console print to mix with print in run()
				TimeUnit.MILLISECONDS.sleep(1);
				System.err.println("File exists");
				errorHandler.sendError(Errors.err_6);
			} else {

				FileOutputStream writeData = new FileOutputStream(path);

				// Send first ACK
				packet.sendACKPacket(blockNumber);

				do {
					// receive data packet
					packet.reciveDataPacket();

					// Get opcode and block number from data packet
					opcode = packet.getOpcode();
					block = packet.getBlock();

					if (opcode == OP_ERR) {
						System.out.println("Error message from client, server exit the connectinon");
						break;
					}

					blockNumber++;

					// Copy received data to byte array, ignoring bytes for opcode and block number
					data = Arrays.copyOfRange(packet.getDataPacket().getData(), 4, packet.getDataPacket().getLength());

					// To be printed in the server console when transmission is successful
					receivedSize += data.length;

					// Check if available space on disk and if not delete the file
					if (DIR_SIZE.getFreeSpace() < (fileToWrite.length() + data.length)) {
						fileToWrite.delete();
						errorHandler.sendError(Errors.err_3);
						System.err.println("Disk is full");
						break;
					}
					// Write data to file
					writeData.write(data);
					writeData.flush();

					// Send ACK
					packet.sendACKPacket(blockNumber);

				} while (data.length > 511 && opcode == OP_DAT && block == blockNumber);

				System.out.println("Received " + receivedSize + " bytes");
				sendSocket.close();
				writeData.close();

			}
		} catch (SocketTimeoutException ste) {
			System.err.println("Transfer timeout");
		} catch (FileNotFoundException fnf) {
			System.err.println("error when creating file in write method " + fnf);
		} catch (InterruptedException ie) {
			System.err.println("Sleep was interupded " + ie);
		} catch (IOException ioe) {
			System.err.println("I/O error when writing to file " + ioe);
			errorHandler.sendError(Errors.err_2);
		}

		return true;
	}
}
