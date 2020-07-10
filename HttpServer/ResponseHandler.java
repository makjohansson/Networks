package mj223vn_assign2;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * A class mapping a HTTP request header and parsing the header and body of a request
 * @author marcus
 *
 */
public class ResponseHandler {
	Map<String, String> responseHeader;
	byte[] fileContentDecodedBytes;
	BufferedReader requestReader;
	StringBuilder bodyBuilder;
	String requestLine;
	String method;
	String location;
	String protocol;
	String fileName;
	String fileContent;
	int contentLength;

	
	/**
	 * Constructor for the class ResponseHandler
	 * @param requestReader a BufferedReader object
	 */
	public ResponseHandler(BufferedReader requestReader) {
		this.requestReader = requestReader;
		responseHeader = new HashMap<String, String>();
	}
	
	/**
	 * Parse a HTTP request, puts the content of HTTP header in to a HashMap<br>
	 * If request method is POST the content of the POST header and body is read character by character in to an array<br> 
	 * From the POST header and body the file name and file content is pinpointed  
	 */
	public void parseRequest() {
		try {
			setRequestLine(requestReader.readLine());
			splitRequestLine();
			mapHeader(requestReader.readLine());

			String header = requestReader.readLine();
			while (header.length() > 0) {
				mapHeader(header);
				header = requestReader.readLine();
			}

			if (method.equalsIgnoreCase("POST")) {
				setContentLength();
				bodyBuilder = new StringBuilder();

				for (int i = 0; i < contentLength; i++) {
					char ch = 0;
					if (requestReader.ready())
						ch = (char) requestReader.read();

					bodyBuilder.append(ch);
				}
				getFilenameAndFileContentfromPostReq();
			}

		} catch (IOException ioe) {
			System.err.println("I/O error reading request to map " + ioe);
		}
	}

	public String getRequestLine() {
		return requestLine;
	}

	public String getHttpMethod() {
		return method;
	}

	public String getHttpLocation() {
		return location;
	}

	public String getHttpProtocol() {
		return protocol;
	}

	public String getSpecificHeaderLine(String line) {
		return responseHeader.get(line);
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileContent() {
		return fileContent;
	}

	public byte[] getPngBase64() {
		return fileContentDecodedBytes;
	}

	public void printMap() {
		responseHeader.entrySet().forEach(header -> {
			System.out.println(header.getKey() + ": " + header.getValue());
		});
	}

	private void setRequestLine(String requestLine) {
		this.requestLine = requestLine;
	}

	private void setContentLength() {
		contentLength = Integer.parseInt(responseHeader.get("Content-Length").trim());
	}

	private void mapHeader(String headerLine) {
		int split = headerLine.indexOf(":");

		responseHeader.put(headerLine.substring(0, split), headerLine.substring(split + 1, headerLine.length()));
	}
	
	/**
	 * Pinpoint file name and file content from a POST request.
	 */
	private void getFilenameAndFileContentfromPostReq() {

		String[] bodyInTwo = bodyBuilder.toString().split("\r\n\r\n");
		// Get the file name in the post request
		String[] splitOne = bodyInTwo[0].split(";");
		String[] splitTwo = splitOne[2].split("\r\n");
		int split = splitTwo[0].indexOf("=");
		fileName = splitTwo[0].substring(split + 1, splitTwo[0].length()).replace("\"", "");

		// Get file content from post request
		splitOne = bodyInTwo[1].split("------WebKitFormBoundary");
		fileContent = splitOne[0].trim();

		if (fileName.endsWith(".png")) {
			String fileContentEncodedTo64 = Base64.getEncoder().encodeToString(fileContent.getBytes());
			fileContentDecodedBytes = Base64.getDecoder().decode(fileContentEncodedTo64);
		}
	}
	
	/**
	 * Used to get the HTTP request method, location and protocol<br>
	 * Also in cases when client browsers send request for favicon.ico, this method makes the server to ignore that request.
	 *  
	 */
	private void splitRequestLine() {
		String[] splitString = requestLine.split("\\s");
		method = splitString[0];
		location = splitString[1];
		protocol = splitString[2];
		// Handle the request favicon.ico from the browser
		if (location.endsWith(".ico"))
			location = "/";

	}

}
