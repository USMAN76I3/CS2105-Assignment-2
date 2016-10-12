import java.net.*;
import java.nio.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.CRC32;

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

	public byte[] longToByte(long value) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(value);
		return buffer.array();
	}

	public long getChecksum(byte[] message) {
		CRC32 crc = new CRC32();
		crc.update(message);
		return crc.getValue();
	}

	public byte[] splitMessage(byte[] message, int start, int end) {
		byte[] packetSize = Arrays.copyOfRange(message, start, end);
		return packetSize;
	}
	
	private byte[] concatenate(byte[] buffer1, byte[] buffer2) {
		byte[] returnBuffer = new byte[buffer1.length + buffer2.length];
		System.arraycopy(buffer1, 0, returnBuffer, 0, buffer1.length);
		System.arraycopy(buffer2, 0, returnBuffer, buffer1.length, buffer2.length);
		return returnBuffer;
	}
	
	public long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(bytes);
		buffer.flip();
		return buffer.getLong();
	}
	
	public byte[] getMessageType(int num) {
		ByteBuffer typeByteArr = ByteBuffer.allocate(4);
		typeByteArr.putInt(num);
		return typeByteArr.array();
	}
	
	public byte[] getSequenceNumber() {
		ByteBuffer seqByteArr = ByteBuffer.allocate(4);
		sequenceNumber++;
		seqByteArr.putInt(sequenceNumber);
		return seqByteArr.array();
	}
	
	int sequenceNumber = 0;

	public void sendMessage(String message, String host, int port) throws Exception {
		// You can assume that a single message is shorter than 750 bytes and
		// thus
		// fits into a single packet.
		// Implement me

		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName(host);

		byte[] messageData = message.getBytes();
		int messageLength = messageData.length;
		int totalPackets = (messageLength / 1000) + 1;

		if (totalPackets == 1) {
			
			byte[] msgAndSeq = concatenate(messageData, getSequenceNumber());
			long checksum = getChecksum(msgAndSeq);
			byte[] checksumByte = longToByte(checksum);
			//System.out.println(bytesToLong(checksumByte));
			byte[] updatedMessage = concatenate(msgAndSeq, checksumByte);
			DatagramPacket sendPacket = new DatagramPacket(updatedMessage, updatedMessage.length, IPAddress, port);
			
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
					} else if (ackedMsg.equals("NAK")) {
						clientSocket.send(sendPacket);
					} else {
						clientSocket.send(sendPacket);
					}
				} catch (SocketTimeoutException e) {
					clientSocket.send(sendPacket);
				}
			}
		} else {
			for (int i = 0; i < totalPackets - 1; i++) {

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

	}
}