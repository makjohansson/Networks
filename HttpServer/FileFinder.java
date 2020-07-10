package mj223vn_assign2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A class returning requested html and png files to a client
 * 
 * @author marcus
 *
 */

public class FileFinder {
	private ResponseMessage respondToClient;
	private String fileLocation;
	private String path;
	private boolean isScaredOfClowns = false;
	private boolean gen500IsTrue = false;

	/**
	 * Constructor for the class FileFinder
	 * 
	 * @param responseToClient is a ResponseMessage object
	 * @param fileLocation     the file requested form client
	 * @param path             is the root directory for the webServer
	 */
	public FileFinder(ResponseMessage responseToClient, String fileLocation, String path) {
		this.respondToClient = responseToClient;
		this.fileLocation = fileLocation;
		this.path = path;
	}

	/**
	 * Use location found in request HTTP header to locate the file to respond to client with.<br>
	 * If location is a directory and a index.html or index.htm exists that file will be the response.<br>
	 * If location is supported file (.png and .html) that file and a 200 OK is the response.<br>
	 * If file not found the response is a 404 status code.
	 */
	public void fileToPrint() {
		if (fileLocation.equalsIgnoreCase("/c"))
			respondToClient.gen302Response();
		puthandler();
		File fileToRead = new File(path + fileLocation);
		System.out.println(fileToRead.getAbsolutePath() + " path");
		if (fileLocation.equalsIgnoreCase("/clown.png") && isScaredOfClowns)
			respondToClient.gen403Response();
		if (fileLocation.equalsIgnoreCase("/a") && gen500IsTrue)
			respondToClient.gen500Response();
		if (fileToRead.isDirectory()) {
			fileLocation = fileLocation + "/index.html";
			fileToRead = new File(path + fileLocation);
			if (!fileToRead.exists()) {
				fileLocation = "/index.htm";
				fileToRead = new File(path + fileLocation);
			}
			if (!fileToRead.exists()) {
				respondToClient.gen404Response();

			}
		}

		if (fileToRead.isFile()) {
			try {
				byte[] responseInBytes = new byte[(int) fileToRead.length()];
				// Open connection to requested file
				FileInputStream readFile = new FileInputStream(fileToRead);

				// Read the file to a byte array
				readFile.read(responseInBytes);
				readFile.close();

				// Generate a OK response
				respondToClient.gen200Response(fileLocation, responseInBytes);

			} catch (FileNotFoundException fnfe) {
				System.err.println("The file requested from client was not found " + fnfe);
			} catch (IOException ioe) {
				System.err.println("I/O error when reading file " + ioe);
			}
		} else {
			System.out.println(" Asshole");
			respondToClient.gen404Response();

		}

	}
	private void puthandler() {
		if(fileLocation.contains("fileToAdd=")) {
			String[] split = fileLocation.split("=");
			fileLocation = "/" + split[1];
			System.out.println(split[1] + " putHandler");
		}
	}

	/**
	 * Set if client is authorized to view the file "clown.png<br>
	 * Default value is always false, meaning that the user is authorized
	 * 
	 * @param isScaredOfClowns boolean set to false if allowed to view file
	 */
	public void setIsScaredOfClowns(boolean isScaredOfClowns) {
		this.isScaredOfClowns = isScaredOfClowns;
	}

	/**
	 * A boolean set to true, response to the client with a status code 500<br>
	 * Used for test cases. fileLocation in request must be /a<br>
	 * If false client will see requested page /a
	 * 
	 * @param gen500IsTrue boolean set to true the status code 500 is returned to
	 *                     client
	 */
	public void setGen500StatusCode(boolean gen500IsTrue) {
		this.gen500IsTrue = gen500IsTrue;
	}

}
