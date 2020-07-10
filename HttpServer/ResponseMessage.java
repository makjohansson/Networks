package mj223vn_assign2;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * A class generating HTTP status codes 
 * @author marcus
 *
 */
public class ResponseMessage {
	private String header;
	private String body;
	private BufferedOutputStream dataOut;
	private byte[] responseHeader;
	private byte[] responseBody;
	private String header200 = "HTTP/1.1 200 OK\r\n";
	
	/**
	 * Constructor for the class ResponseMessage
	 * @param dataOut a BufferedOutputStream object 
	 */
	public ResponseMessage(BufferedOutputStream dataOut) {
		this.dataOut = dataOut;

	}
	
	// The HTTP status code that will be generated:

	public void gen404Response() {
		body = "<html><head><title>Not Found</title></head><body><h1>404 Not Found</h1></body></html>";
		header = "HTTP/1.1 404 Not Found\r\n" + "Content-Type: text/html\r\n" + "Content-Length: " + bodyLength()
				+ "\r\n" + "Date: " + new Date() + "\r\n\r\n";
		respondToClient();
	}

	public void gen201Response() {
		body = "<html><head><title>Created</title></head><body><h1>201 Created</h1></body></html>";
		header = "HTTP/1.1 201 Created\r\n" + "Content-Type: text/html\r\n" + "Content-Length: " + bodyLength() + "\r\n"
				+ "Date: " + new Date() + "\r\n\r\n";
		respondToClient();
	}

	public void gen302Response() {
		header = "HTTP/1.1 302 Found\r\n" + "Location: /clown.png\r\n\r\n";
		System.out.println("Client was redirected to /clown.png");
		respondToClient();
	}

	public void gen403Response() {
		body = "<html><head><title>Forbidden</title></head><body><h1>403 Forbidden</h1></body></html>";
		header = "HTTP/1.1 403 Forbidden\r\n" + "Content-Type: text/html\r\n" + "Content-Length: " + bodyLength()
				+ "\r\n" + "Date: " + new Date() + "\r\n\r\n";
		System.out.println("Client is afraid of clown, to change this set the boolean \"isScaredOfClowns\" to false");
		respondToClient();
	}
	
	public void gen405Response() {
		body = "<html><head><title>Method Not Allowed</title></head><body><h1>404 Method Not Allowed</h1></body></html>";
		header = "HTTP/1.1 404 Method Not Allowed\r\n" + "Content-Type: text/html\r\n" + "Content-Length: " + bodyLength()
				+ "\r\n" + "Date: " + new Date() + "\r\n\r\n";
		respondToClient();
	}
	
	public void gen409Response() {
		body = "<html><head><title>Conflict</title></head><body><h1>409 Conflict</h1><br><h4>File already exists</h4></body></html>";
		header = "HTTP/1.1 409 Conflict\r\n" + "Content-Type: text/html\r\n" + "Content-Length: " + bodyLength()
				+ "\r\n" + "Date: " + new Date() + "\r\n\r\n";
		respondToClient();
	}

	public void gen500Response() {
		body = "<html><head><title>Server Error</title></head><body><h1>500 Internal Server Error</h1></body></html>";
		header = "HTTP/1.1 500 Internal Server Error\r\n" + "Content-Type: text/html\r\n" + "Content-Length: "
				+ bodyLength() + "\r\n" + "Date: " + new Date() + "\r\n\r\n";
		respondToClient();
	}

	/**
	 * Respond to a client with the 200 OK status code, generates HTTP header for supported file (.png and .html)<br>
	 * The response is sent to the client in bytes.
	 * @param fileLocation the filename to present to the client
	 * @param responseInBytes the content of the file
	 */
	public void gen200Response(String fileLocation, byte[] responseInBytes) {
		// Generate the correct response header for a OK html or htm file 
		if(fileLocation.endsWith(".html") || fileLocation.endsWith(".htm")) {
			header200 += "Content-Type: text/html\r\n" + "Content-Length: " + responseInBytes.length + "\r\n"
					+ "Date: " + new Date() + "\r\n\r\n";
		}
		// Generates the correct header for a OK png file
		else if(fileLocation.endsWith(".png")) {
			header200 += "Content-Type: image/png\r\n" + "Content-Length: " + responseInBytes.length + "\r\n"
					+ "Date: " + new Date() + "\r\n\r\n";
		}
		// Generates the correct response header for file's with binary data that doesn't fall into one of the other types
		else {
			header200 += "Content-Type: application/octet-stream\r\n" + "Content-Length: " + responseInBytes.length + "\r\n"
					+ "Date: " + new Date() + "\r\n\r\n";
		}
		
		byte[] headerInByte = header200.getBytes(); 
		
		try {
			// respond with the header info to the client
			dataOut.write(headerInByte, 0, headerInByte.length);
			
			// Forces the output stream to writ out the remaining bytes
			dataOut.flush();
			
			// respond to client with the content in body
			dataOut.write(responseInBytes, 0, responseInBytes.length);

			// Forces the output stream to writ out the remaining bytes
			dataOut.flush();
			
		} catch (IOException ioe) {
			System.err.println("I/O error when trying sending 200 OK response to client " + ioe);
		}	
	}

	private int bodyLength() {
		return body.length();
	}

	private boolean bodyIsInResponse() {
		return body != null;
	}
	
	/**
	 * Use a HTTP header and  HTML body to response to a client.<br>
	 * A BufferedOutputStream object is used to send the response to the client in bytes.
	 */
	private void respondToClient() {
		responseHeader = header.getBytes();
		if (bodyIsInResponse())
			responseBody = body.getBytes();

		try {
			dataOut.write(responseHeader, 0, responseHeader.length);
			dataOut.flush();
			if (bodyIsInResponse()) {
				dataOut.write(responseBody, 0, responseBody.length);
				dataOut.flush();
			}
			dataOut.close();
		} catch (IOException ioe) {
			System.out.println("I/O error responding to the client " + ioe);
		}
	}
}
