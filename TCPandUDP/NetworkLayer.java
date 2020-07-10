package mj223vn_assign1;

/**
 * This is abstract class used by a UDP and a TCP client.
 * The two clients use the setupConnection to get the required data to connect to a server.
 * Error for the input data in the args[] are handled by methods defined in this class.  
 */

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class NetworkLayer {

	protected int bufSize = 1024;
	protected int transferRate;
	protected String IP;
	protected int port = 4950;
	protected final int MYPORT = 0;
	protected final String MSG = "An Echo Message!";
	protected byte[] buf;
	protected SocketAddress localBindPoint;
	protected SocketAddress remoteBindPoint;
	protected String echo = "";
	protected boolean underOneSecond;
	protected long oneSecond;
	protected int messagesSent = 0;
	protected int messagesLeftToSend;
	protected int messagesSentNotEqual;
	protected long startTime;
	protected boolean transferRateZero = true;
	private String[] dots;


	/**
	 * Needs 4 arguments to run
	 * 
	 * @param args
	 */
	public NetworkLayer(String[] args) {
		setupConnection(args);
	}

	/**
	 * Setup for a UDP Client and a TCP Client Needs 4 arguments to run
	 * 
	 * @param args
	 */
	private void setupConnection(String[] args) {
		try {
			IP = args[0];
			port = Integer.valueOf(args[1]);
			bufSize = Integer.valueOf(args[2]);
			buf = new byte[bufSize];
			transferRate = Integer.valueOf(args[3]);
			isValidData();
			localBindPoint = new InetSocketAddress(MYPORT);
			remoteBindPoint = new InetSocketAddress(IP, port);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Enter arguments: \'IP\' \'Port\' \'Buffer size\' \'Transfer rate\'");
			System.exit(1);
		}
	}

	/**
	 * The run() method that is implemented different for UDP and TCP
	 */
	public abstract void run();

	/**
	 * Compare a string with the MSG string
	 * 
	 * @param receivedString
	 */
	protected void compareSentAndReceviedMessage(String receivedString) {
		if (receivedString.compareTo(MSG) != 0)
			System.out.printf("\n\t-----Sent and received msg not equal!-----");
		else
			return;
	}

	/**
	 * check if all messages in the run method have been sent under one second. If
	 * so the method will delay continuing for the remaining time and then print the
	 * amount of messages sent. If not the counters messageSent and
	 * messagesLeftToSend is printed. Every case resetMessages() is called. If
	 * transferRate == 0 on print will be done before shutting down the program.
	 */
	protected void oneSecondChecker() {
		try {
			if (oneSecond > System.currentTimeMillis()) {
				Thread.sleep(oneSecond - System.currentTimeMillis());
				System.out.printf("\n\t----- One Second! %d messages sent -----", messagesSent);
				resetMessages();
			} else {
				underOneSecond = false;
				System.out.printf(
						"\n\n\t-----\t      %d messages sent!     -----\n\t-----    %d messages left to send   -----\n",
						messagesSent, messagesLeftToSend);
				resetMessages();
			}
			if (transferRate == 0)
				System.exit(1);

		} catch (InterruptedException e) {
			System.err.println("You been interrupted " + e);
		}
	}

	/**
	 * Set messagesLeftToSend and messagesSent to zero
	 */
	protected void resetMessages() {
		messagesLeftToSend = 0;
		messagesSent = 0;
		messagesSentNotEqual = 0;
	}

	/**
	 * Check if the arguments data is valid. If not the program is shutdown and a
	 * proper error message is printed. If valid
	 * 
	 * @return true
	 */
	protected boolean isValidData() {
		if (!validIP()) {
			System.err.printf("Invalid IP: %s", IP);
			System.exit(1);
		}
		if (!validPort()) {
			System.err.printf("Invalid Port number: %d", port);
			System.exit(1);
		}
		if (!validBufSize()) {
			System.err.printf("Invalid buffer size: %d", bufSize);
			System.exit(1);
		}
		if (!validTransferRate()) {
			System.err.printf("Invalid transfer rate: %d", transferRate);
			System.exit(1);
		}
		return true;
	}

	/*
	 * Check if a valid IP
	 */
	private boolean validIP() {
		try {
			if (IP == null)
				return false;
			dots = IP.split("\\.");
			if (dots.length != 4)
				return false;
			for (String s : dots) {
				int number = Integer.parseInt(s);
				if (number < 0 || number > 255)
					return false;
			}
			if (IP.endsWith("."))
				return false;

			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	/*
	 * CHeck if valid Port
	 */
	private boolean validPort() {
		if (port < 0 || port > 65535) {
			return false;
		}
		return true;
	}

	/*
	 * Check if valid buffer size
	 */
	private boolean validBufSize() {
		if (bufSize < 1 || bufSize > 65507) {
			return false;
		}
		return true;
	}

	private boolean validTransferRate() {
		if (transferRate < 0)
			return false;
		else
			return true;
	}

}
