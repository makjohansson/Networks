package mj223vn_assign1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * A class handling UDP Clients
 * @param args
 * @throws IOException
 */
public class UDPEchoServer {
	public static int bufSize = 1024;
	public static final int MYPORT = 4950;

	public static void main(String[] args) throws IOException {
		try {
		byte[] buf = new byte[Integer.valueOf(args[0])];
		System.out.printf("Buffer size: %s\n", buf.length);

		/* Create socket */
		@SuppressWarnings("resource")
		DatagramSocket socket = new DatagramSocket(null);

		/* Create local bind point */
		SocketAddress localBindPoint = new InetSocketAddress(MYPORT);
		socket.bind(localBindPoint);
		while (true) {
			/* Create datagram packet for receiving message */
			DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

			/* Receiving message */
			socket.receive(receivePacket);

			/* Create datagram packet for sending message */
			DatagramPacket sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
					receivePacket.getAddress(), receivePacket.getPort());

			/* Send message */
			try {
				socket.send(sendPacket);
			} catch(IOException ioe) {
				System.err.println("I/O error " + ioe);
			}
			
			System.out.printf("UDP echo request from %s", receivePacket.getAddress().getHostAddress());
			System.out.printf(" using port %d\n", receivePacket.getPort());
		}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Add argumetn for buffer size!");
		} catch (SocketException se) {
			System.err.println("Socket could not be opened");
		}
	}
}