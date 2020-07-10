package mj223vn_assign3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Package and send package to client
 * @author Marcus Johansson
 *
 */
public class Packet {
	private Sender send;
	private DatagramSocket sendSocket;
	private DatagramPacket dataPacket;
	private ByteBuffer sendFile;
	private byte[] buf;
	private short opcode;
	private short block;
	private final int OP_DAT = 3;
	private final int OP_ACK = 4;
	
	/**
	 * Create a package to be sent to a TFTP client
	 * @param sendSocket
	 */
	public Packet(DatagramSocket sendSocket) {
		this.sendSocket = sendSocket;
		this.send = new Sender(sendSocket);
		
	}
	
	/**
	 * Receive a DATA packet of size 512 bytes and parse packet for OPcode and block number
	 * @throws IOException if something goes wrong with the socket
	 */
	public void reciveDataPacket() throws IOException {
		buf = new byte[516];
		dataPacket = new DatagramPacket(buf, buf.length);
		sendSocket.receive(dataPacket);
		parse();
	}
	
	/**
	 * Send TFTP package to client with the OPcode(3), block number and the data as bytes
	 * @param blockNumber number of block
	 * @param readBytes data to be send in a byte array
	 */
	public void sendDataPacket(int blockNumber, byte[] readBytes) {
		int size = readBytes.length;
		sendFile = ByteBuffer.allocate(4 + size);
		sendFile.putShort((short) OP_DAT);
		sendFile.putShort((short) blockNumber);
		sendFile.put(readBytes);
		send.send(sendFile.array());
	}
	
	/**
	 * Receive OPcode(4) ACK from client
	 * @throws IOException if something is wrong with the socket
	 */
	public void receiveACKPacket() throws IOException {
		buf = new byte[4];
		DatagramPacket AckPacket = new DatagramPacket(buf, buf.length);
		sendSocket.receive(AckPacket);
		parse();
	}
	
	/**
	 * Send OPcode(4) ACK to client 
	 * @param blockNumber number on block to be sent
	 */
	public void sendACKPacket(int blockNumber) {
		ByteBuffer sendACK = ByteBuffer.allocate(OP_ACK + blockNumber);
		sendACK.putShort((short) OP_ACK);
		sendACK.putShort((short) blockNumber);
		send.send(sendACK.array());
	}

	public DatagramPacket getDataPacket() {
		return dataPacket;
	}

	public short getOpcode() {
		return opcode;
	}

	public short getBlock() {
		return block;
	}
	
	public InetAddress getAddress() {
		return sendSocket.getInetAddress();
	}
	
	/**
	 * Set OPcode and block number
	 */
	private void parse() {
		ByteBuffer wrap = ByteBuffer.wrap(buf);
		opcode = wrap.getShort();
		block = wrap.getShort();
	}
}
