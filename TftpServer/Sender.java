package mj223vn_assign3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Used to send package to TFTP client
 * @author Marcus Johansson
 *
 */
public class Sender {
	private DatagramSocket socket;
	
	public Sender(DatagramSocket socket) {
		this.socket = socket;
	}
	
	/**
	 * Send byte array to client
	 * @param buf byte array to send
	 */
	public void send(byte[] buf) {
		try {
			socket.send(new DatagramPacket(buf, buf.length));
		} catch (IOException ioe) {
			System.err.println("Error sending packet " + ioe);
		}
	}
}
