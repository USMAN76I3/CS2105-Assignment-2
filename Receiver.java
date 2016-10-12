import java.net.*;
import java.nio.*;



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
  
  public String receiveMessage(DatagramSocket socket) throws Exception {
      // Implement me
	  
	  byte[] inBuffer = new byte[1024];
	  DatagramPacket receivedPacket = new DatagramPacket(inBuffer, inBuffer.length);
	  socket.receive(receivedPacket);
	  
	  String ACKED = "ACK";
	  String receivedData = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
	  InetAddress senderAddress = receivedPacket.getAddress();
	  int senderPort = receivedPacket.getPort();
	  
	  byte[] outBuffer = ACKED.getBytes();
	  DatagramPacket sendPacket = new DatagramPacket(outBuffer, outBuffer.length, senderAddress, senderPort);
	  
	  socket.send(sendPacket);
	  return receivedData;
  }
}