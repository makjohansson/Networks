package mj223vn_assign2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Class used to save files from POST and PUT requests
 * @author marcus
 *
 */

public class SaveFilesToServer {
	private ResponseHandler getResponse;
	ResponseMessage respondToClient;
	private String path;
	
	/**
	 * Constructor for the class SaveFilesToServer
	 * @param getResponse is a ResponseHandler object
	 * @param respondToClient is a ResponseMessage object
	 * @param path is the root directory for the webServer
	 */
	public SaveFilesToServer(ResponseHandler getResponse, ResponseMessage respondToClient, String path) {
		this.getResponse = getResponse;
		this.respondToClient = respondToClient;
		this.path = path;
	}
	
	/**
	 * This method save the file that a client requested to save on server<br>
	 * If request is POST and file exists on server the client gets a 409 response
	 */
	public void saveOnServer() {
		try {
			String file = path + "/" + getResponse.getFileName();
			File image = new File(file);
			if (image.exists() && getResponse.method.equalsIgnoreCase("POST")) {
				respondToClient.gen409Response();
			} else {
				FileOutputStream saveFile = new FileOutputStream(file);
				if (getResponse.getFileName().endsWith(".png"))
					saveFile.write(getResponse.getPngBase64());
				else
					saveFile.write(getResponse.getFileContent().getBytes());
				System.out.println("File was saved to server");
				saveFile.close();
			}
		} catch (FileNotFoundException fnf) {
			System.err.println("File to save on server was not found " + fnf);

		} catch (IOException ioe) {
			System.err.println("I/O error when saving file to server " + ioe);
		}
	}
}
