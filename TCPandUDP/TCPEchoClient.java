package mj223vn_assign1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Class that sends TCP packets
 * @author marcus
 *
 */
public class TCPEchoClient extends NetworkLayer {

	private Socket socket;
	
	/**
	 * Needs 4 arguments to run. 
	 * IP, port number, buffer size and transfer rate. In that order
	 * @param args
	 */
	public TCPEchoClient(String[] args) {
		super(args);
	}

	/**
	 * If the four arguments data is valid, this method set up a stream to send and receive an echo message by a transfer rate specified by the user.
	 * The transfer rate is in messages per second. 
	 * @throws SocketException if the is no server running 
	 * @throws IOException if an I/O occurs
	 */
	public void run() {
		try {
			/* Create TCP socket and connect to endpoint */
			socket = new Socket(IP, port);
			OutputStream messageToServer = socket.getOutputStream();
			InputStream echoMessageFromServer = socket.getInputStream();
			while (isValidData()) {
				underOneSecond = true;
				startTime = System.currentTimeMillis();
				oneSecond = startTime + 1000;
				int br = 0;
				String theFullEchoMessageRecived = "";
				/* Runs for one second */
				while (System.currentTimeMillis() < oneSecond && underOneSecond) {
					
					for (int i = 0; i < transferRate; i++) {
						/* Write the bytes in the message to the Output stream*/
						messageToServer.write(MSG.getBytes());
						echo = "";
						messagesSent++;
						messagesLeftToSend = transferRate - messagesSent;
						if (System.currentTimeMillis() > oneSecond || messagesSent >= transferRate)
							break;
						do {
							buf = new byte[bufSize];
							/* Reads the Input stream and put the bytes received in a buffer and returns the number of bytes read as an integer  */
							br = echoMessageFromServer.read(buf);
							/* Concatenate a string with the bytes read  */
							echo += new String(buf, 0, br);
						}
						while (echo.length() < MSG.length());					
						theFullEchoMessageRecived = echo;
					}
					oneSecondChecker();
					compareSentAndReceviedMessage(theFullEchoMessageRecived);
					System.out.println("\n\t\t   " + theFullEchoMessageRecived);
				}				
			}
			socket.close();
		} catch (SocketException se) {
			System.err.println("Server is not runnig!");
			System.exit(1);
		} catch (IOException ioe) {
			System.err.println("I/O error " + ioe);
		}
	}
}
