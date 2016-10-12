import java.net.*;
import java.nio.*;
import java.util.Arrays;
import java.util.zip.CRC32;

class Receiver {
	public static void main(String[] args) throws Exception {
		// check if the number of command line argument is 4
		if (args.length != 1) {
			System.out.println("Usage: java Receiver <port>");
			System.exit(1);
		}
		new Receiver(Integer.parseInt(args[0]));
	}

	public Receiver(int port) throws Exception {
		// Do not change this
		DatagramSocket socket = new DatagramSocket(port);
		while (true) {
			String message = receiveMessage(socket);
			System.out.println(message);
		}
	}

	public long getChecksum(byte[] message) {
		CRC32 crc = new CRC32();
		crc.update(message);
		return crc.getValue();
	}
	
	public long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(bytes);
		buffer.flip();
		return buffer.getLong();
	}

	public boolean verifyCS(byte[] message, int size) {
		byte[] messageBytes = Arrays.copyOfRange(message, 0, size - 8);
		byte[] checksumBytes = Arrays.copyOfRange(message, size - 8, size);
		long messageCS = getChecksum(messageBytes);
		long checksum = bytesToLong(checksumBytes);
		return messageCS == checksum;
	}
	
	public int getSequenceNumber(byte[] message, int size) {
		byte[] seqNumArr = Arrays.copyOfRange(message, size - 12, size -8);
		return ByteBuffer.wrap(seqNumArr).getInt();

	}
	
	int sequenceNumber = 0;

	public String receiveMessage(DatagramSocket socket) throws Exception {
		// Implement me

		byte[] inBuffer = new byte[1008];
		DatagramPacket receivedPacket = new DatagramPacket(inBuffer, inBuffer.length);
		socket.receive(receivedPacket);
		// Packet Received
		
		int packetSize = receivedPacket.getLength();
		byte[] packetData = receivedPacket.getData();
		InetAddress senderAddress = receivedPacket.getAddress();
		int senderPort = receivedPacket.getPort();
		//System.out.println(verifyCS(packetData, packetSize));
		
		if (verifyCS(packetData, packetSize)) {
			String receivedData = new String(receivedPacket.getData(), 0, packetSize - 12);
			int seqNum = getSequenceNumber(receivedPacket.getData(), packetSize);
			
			if (seqNum == sequenceNumber) {
				String ACKED = "ACK";
				byte[] outBuffer = ACKED.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(outBuffer, outBuffer.length, senderAddress, senderPort);

				socket.send(sendPacket);
				return receiveMessage(socket);
			}

			String ACKED = "ACK";
			byte[] outBuffer = ACKED.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(outBuffer, outBuffer.length, senderAddress, senderPort);
			sequenceNumber = seqNum;

			socket.send(sendPacket);
			return receivedData;
		} else {
			String NAKED = "NAK";
			byte[] outBuffer = NAKED.getBytes();
			
			DatagramPacket sendPacket = new DatagramPacket(outBuffer, outBuffer.length, senderAddress, senderPort);
			
			socket.send(sendPacket);
			return receiveMessage(socket);
		}
		
		 //should never reach here
	}
}