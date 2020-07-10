package mj223vn_assign1;

/**
 * Main class to run A UDPEchoClient or a TCPEchoCLient
 * @author marcus
 *
 */

public class RunClient {
	
	public static void main(String[] args) {
		
		
		NetworkLayer client =new UDPEchoClient(args);
		//NetworkLayer client =new TCPEchoClient(args);
		
		client.run();
	}

}
