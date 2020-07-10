package mj223vn_assign1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * A server class handling TCP Clients using Threads 
 * @author marcus
 *
 */
public class TCPEchoServer {

	public static final int MYPORT = 4950;
	

	public static void main(String[] args) {
		
		
		try {
			byte[] buf = new byte[Integer.valueOf(args[0])];
			System.out.printf("Buffer size: %s", buf.length);
			@SuppressWarnings("resource")
			ServerSocket ss = new ServerSocket(MYPORT);
			ss.setReuseAddress(true);
			while(true) {
				Socket client = ss.accept();
				System.out.printf("TCP echo request from %s", client.getInetAddress().getHostAddress());
				System.out.printf(" using port %d\n", client.getPort());
				
				
				ClientHandler serverThread  = new ClientHandler(client, buf);
				new Thread(serverThread).start();
				
				
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			System.err.println("Add argument for buffer size");
		} catch (IOException ioe) {
			System.err.println("I/O error " + ioe);
		}
		
	}

	private static class ClientHandler implements Runnable {
		private final Socket clientSocket;
		private byte[] buf;
		private DataOutputStream out;
		private DataInputStream in;
		private volatile boolean exit = false;
		private String receivedPacket;
		private int messageCounter = 0;
		private int br;

		public ClientHandler(Socket socket, byte[] buf) {
			this.clientSocket = socket;
			this.buf = buf;

			try {
				out = new DataOutputStream(clientSocket.getOutputStream());
				in = new DataInputStream(clientSocket.getInputStream());
			} catch (IOException ioe) {
				System.err.println("I/O error " + ioe);
			}
		}
		
		
		public void run() {
			try {
				while (!exit) {
					/*Number of bytes read return as a integer*/
					br = in.read(buf);
					/* Buffer casted to a sting*/
					receivedPacket = new String(buf);
					if (receivedPacket.isEmpty())
						exit = true;
					else {
						/*Write bytes to the Output stream from the beginning of buf and ends at the number stored in br*/
						out.write(buf, 0, br);
						/*Forces any buffered bytes to be written to the stream*/
						out.flush();
						messageCounter++;				 
					}
				}
			} catch(SocketException se) {
				System.err.printf("Client disconected, messages sent: %d\n", messageCounter);
				exit = true;
			}catch (IOException ioe) {
				close();
				System.err.println("I/O error " + ioe);
			}
			close();

		}
		
		/**
		 * Close the socket and print IP of the Client socket
		 */
		private void close() {
			try {
				clientSocket.close();
				System.err.printf("Socket connect to %s is closed \n\n" , clientSocket.getInetAddress().getHostAddress());
			} catch(SocketException se) {
				System.err.println("Socker error " + se);
			} catch(IOException ioe) {
				System.err.println("I/O error " + ioe);
			}
		}
	}
}
