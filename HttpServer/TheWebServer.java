package mj223vn_assign2;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TheWebServer {
	public static String path;
	public static int port;

	public static void main(String[] args) {
		port = Integer.parseInt(args[0]);
		if (!validPort()) {
			System.err.printf("Invalid Port number: %d", port);
			System.exit(1);
		}
		path = args[1];

		final ServerSocket serverSocket;
		Socket socket;

		try {

			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			System.out.println("Server is running...");
			while (true) {
				socket = serverSocket.accept();

				ClientHandler serverThread = new ClientHandler(socket);
				new Thread(serverThread).start();

			}
		} catch (IOException ioe) {
			System.err.println("I/O error opening socket " + ioe);
		}

	}
	/**
	 * Check if port number in args[0] is valid, 0 < port > 65535
	 * @return true if valid port number
	 */
	private static boolean validPort() {
		if (port < 0 || port > 65535) {
			return false;
		}
		return true;
	}

	private static class ClientHandler implements Runnable {
		private volatile boolean exit = false;
		private Socket socket;
		private BufferedOutputStream dataOut;
		private final String GET_REQUEST = "GET";
		private final String POST_POSTREQUEST = "POST";
		private final String PUT_REQUEST = "GET";
		private String method;
		private ResponseHandler getResponse;
		private ResponseMessage respondToClient;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			while (!exit) {
				System.out.println("Connected to: " + socket.getLocalSocketAddress());

				try {
					// Get chars from the client with a request
					BufferedReader requestReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					getResponse = new ResponseHandler(requestReader);

					getResponse.parseRequest();
					// Split the request in Method, file location and Protocol
					String fileLocation = getResponse.getHttpLocation();

					method = getResponse.getHttpMethod();
					// Get the stream to write file to
					dataOut = new BufferedOutputStream(socket.getOutputStream());
					respondToClient = new ResponseMessage(dataOut);
					// The request is a GET request
					if (method.equalsIgnoreCase(GET_REQUEST)) {

						// Find file on server
						FileFinder findFile = new FileFinder(respondToClient, fileLocation, path);
						findFile.setIsScaredOfClowns(false);
						findFile.setGen500StatusCode(false);
						findFile.fileToPrint();
						close();
					}
					// The request is a POST request
					else if (method.equalsIgnoreCase(POST_POSTREQUEST) || method.equalsIgnoreCase(PUT_REQUEST)) {
						// Save file to server
						SaveFilesToServer save = new SaveFilesToServer(getResponse, respondToClient, path);
						save.saveOnServer();
						// Respond to client
						respondToClient.gen201Response();
						close();

					} else {
						System.out.println(method);
						respondToClient.gen404Response();
						close();
					}
				} catch (IOException ioe) {
					respondToClient.gen500Response();
					close();
					System.err.println("I/O error no.1 " + ioe);
				}

			}
		}

		private void close() {
			try {
				socket.close();
				exit = true;
			} catch (IOException ioe) {
				System.err.println("I/O error when closing socket " + ioe);
			}
		}
	}

}
