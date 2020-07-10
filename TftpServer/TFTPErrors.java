package mj223vn_assign3;

import java.net.DatagramSocket;
import java.nio.ByteBuffer;

/**
 * Handles the error codes included in the TFTP protocol
 * @author Marcus Johansson
 *
 */
public class TFTPErrors {
	private Sender send;
	private Errors error;
	private final int OP_ERR = 5;
	private ByteBuffer errorBuf;
	private boolean validMode;
	public enum Errors {
		err_0, err_1, err_2, err_3, err_4, err_5, err_6, err_7
	}

	/**
	 * Sends error code included in the TFTP protocol using enums in the order of error code found in RFC1350 doc
	 * @param sendSocket socket to use to send error code
	 * @param validMode true if TFTP mode is valid on the server
	 */
	public TFTPErrors(DatagramSocket sendSocket, boolean validMode) {
		this.validMode = validMode;
		send = new Sender(sendSocket);
	}

	public void sendError(Errors error) {
		this.error = error;
		send_ERR();
	}

	/*
	 * Generate error messages according to the TFTP formats
	 * 
	 * |Opcode 2 bytes | ErrorCode 2 bytes | ErrorMessage string | 0 1 byte |
     * 
	 */
	private void send_ERR() {
		int errorCode = 0;
		String errorMSG = "";
		switch (error) {
		case err_0:
			errorMSG = "Not defined, see error message (if any)";
			if(validMode == false)
				errorMSG += "\n\t\tTFTP mode used is not allow on this server";
			errorCode = 0;
			break;
		case err_1:
			errorMSG = "File Not Found";
			errorCode = 1;
			break;
		case err_2:
			errorMSG = "Access violation";
			errorCode = 2;
			break;
		case err_3:
			errorMSG = "Disk full or allocation exceeded";
			errorCode = 3;
			break;
		case err_4:
			errorMSG = "Illegal TFTP operation";
			errorCode = 4;
			break;
		case err_5:
			errorMSG = "Unknown transfer ID";
			errorCode = 5;
			break;
		case err_6:
			errorMSG = "File already exist";
			errorCode = 6;
			break;
		case err_7:
			errorMSG = "No such user";
			errorCode = 7;
			break;
		}
		errorBuf = ByteBuffer.allocate(OP_ERR + errorCode + errorMSG.length() + 1);
		errorBuf.putShort((short) OP_ERR);
		errorBuf.putShort((short) errorCode);
		errorBuf.put(errorMSG.getBytes());
		send.send(errorBuf.array());
	}
}
