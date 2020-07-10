package mj223vn_assign1;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Class that sends UDP packets in
 * 
 * @author marcus
 *
 */
public class UDPEchoClient extends NetworkLayer {

	private DatagramSocket socket;
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;
	private final int TIME_OUT = 2000;

	/**
	 * Needs 4 arguments to run. IP, port number, buffer size and transfer rate. In
	 * that order
	 * 
	 * @param args
	 */
	public UDPEchoClient(String[] args) {
		super(args);
	}

	/**
	 * If the four arguments data is valid, this method send and receive UDP packets
	 * at a transfer rate specified by the user. The transfer rate is in messages
	 * per second.
	 * 
	 * @throws SocketTimeoutException if the receive method takes longer than value
	 *                                of TIME_OUT
	 * @throws BindException          if the Port already is in use
	 * @throws SocketException        If the socket could not be opened
	 * @throws IOException            if an I/O occurs
	 */
	public void run() {
		while (isValidData() && transferRateZero) {
			underOneSecond = true;
			startTime = System.currentTimeMillis();
			oneSecond = startTime + 1000;
			while (System.currentTimeMillis() < oneSecond && underOneSecond) {
				for (int i = -1; i < transferRate; i++) {
					try {
						socket = new DatagramSocket(localBindPoint);
						socket.setSoTimeout(TIME_OUT);
						sendPacket = new DatagramPacket(MSG.getBytes(), MSG.length(), remoteBindPoint);
						receivePacket = new DatagramPacket(buf, buf.length);
						socket.send(sendPacket);
						socket.receive(receivePacket);
						messagesSent++;
						messagesLeftToSend = transferRate - messagesSent;
						compareSentAndReceviedMessage(new String(receivePacket.getData(), receivePacket.getOffset(),
								receivePacket.getLength()));
						socket.close();
						if (System.currentTimeMillis() > oneSecond || messagesSent >= transferRate)
							break;

					} catch (SocketTimeoutException sto) {
						System.err.println("\n\n\t\tRecive timeout after " + TIME_OUT / 1000 + " seconds");
					} catch (BindException be) {
						System.err.println("Service is already running on port " + port);
					} catch (SocketException se) {
						System.err.println("Can't access socket!");
					} catch (IOException ioe) {
						System.err.println("I/O error " + ioe);
					}

				}
				oneSecondChecker();

			}
		}
	}
}
