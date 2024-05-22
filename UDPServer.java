import java.net.DatagramSocket; // Import DatagramSocket
import java.net.DatagramPacket; // Import DatagramPacket
import java.net.InetAddress; // InetAddress object

public class UDPServer {

    public static void main(String[] args) throws Exception{
        int port = 0;
        int pingCounter = 0; // Initialize a counter for pings

        /** Parse port number from command line **/
        try {
            port = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Need one argument: port number.");
            System.exit(-1);
        } catch (NumberFormatException e) {
            System.out.println("Please give port number as integer.");
            System.exit(-1);
        }
        try {
            // Create a DatagramSocket to listen on the specified port.
            DatagramSocket serverSocket = new DatagramSocket(port);

            System.out.println("The UDP server is listening on port " + port);

            while (true) {
                /** Create a new datagram packet and let the socket receive it **/
                byte[] receiveData = new byte[512];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                /** Print the message received **/
                System.out.println("PING " + pingCounter + " " + receivedMessage);
                // Increment the counter for the next ping
                pingCounter++;
                /** Get the IP Address of the Sender **/
                String senderIPAddress = receivePacket.getAddress().getHostAddress();
                /** Get the port of the Sender **/
                int senderPort = receivePacket.getPort();
                /** Prepare the data to send back **/
                byte[] sendData = receivedMessage.getBytes();
                // Create a new datagram packet to send the response
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(senderIPAddress), senderPort);
                /** Send the packet **/
                serverSocket.send(sendPacket);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
