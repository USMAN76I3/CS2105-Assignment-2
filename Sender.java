import java.net.*;
import java.nio.*;
import java.util.Scanner;

class Sender {
	public static void main(String[] args) throws Exception {
		// check if the number of command line argument is 4
		if (args.length != 1) {
			System.out.println("Usage: java Sender <unreliNetPort>");
			System.exit(1);
		}
		new Sender("localhost", Integer.parseInt(args[0]));
	}

	public Sender(String host, int port) throws Exception {
		// Do not change this
		Scanner sc = new Scanner(System.in);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			sendMessage(line, host, port);
			// Sleep a bit. Otherwise sunfire might get so busy
			// that it actually drops UDP packets.
			Thread.sleep(20);
		}
	}

	public void sendMessage(String message, String host, int port) throws Exception {
		// You can assume that a single message is shorter than 750 bytes and
		// thus
		// fits into a single packet.
		// Implement me

		DatagramSocket clientSocket = new DatagramSocket();

		byte[] messageData = message.getBytes();
		InetAddress IPAddress = InetAddress.getByName(host);
		DatagramPacket sendPacket = new DatagramPacket(messageData, messageData.length, IPAddress, port);

		clientSocket.send(sendPacket);

		byte[] inBuffer = new byte[1024];
		DatagramPacket receivedPacket = new DatagramPacket(inBuffer, inBuffer.length);

		boolean NOTACKED = true;
		while (NOTACKED) {
			try {
				clientSocket.setSoTimeout(2000);
				clientSocket.receive(receivedPacket);
				
				String ackedMsg = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
				if (ackedMsg.equals("ACK")) {
					NOTACKED = false;
				}
			} catch (SocketTimeoutException e) {
				clientSocket.send(sendPacket);
			}
		}

	}
}