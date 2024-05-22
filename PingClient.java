import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;


public class PingClient {
    /** Socket which we use. */
    DatagramSocket socket;
    /** Set maximum length of a ping message to 512. */
    public static final int MAX_PING_MESSAGE_LENGTH = 512;
    /** Create a datagram socket with random port for sending UDP messages */
    public void createSocket() {
        try {
            socket = new DatagramSocket(); // No argument means any available port
        } catch (SocketException e) {
            System.out.println("Error creating socket: " + e);
        }
    }
    /** Create a datagram socket for receiving UDP messages.
     * This socket must be bound to the given port. */
    public void createSocket(int port) {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println("Error creating socket: " + e);
        }
    }
    /** Send a UDP ping message which is given as the argument. */
    public void sendPing(Message ping) {
        try {
            /* Create a datagram packet addressed to the recipient */
            InetAddress host = ping.getIP();
            int port = ping.getPort();
            String messageContent = ping.getContents();
            byte[] sendData = messageContent.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, host, port);
            /* Send the packet */
            socket.send(sendPacket);
            System.out.println("Sent message to " + host + ":" + port);
        } catch (IOException e) {
            System.out.println("Error sending packet: " + e);
        }
    }


    /** Receive a UDP ping message and return the received message.
     * We throw an exception to indicate that the socket timed out.
     * This can happen when a message is lost in the network. */
    public Message receivePing() throws SocketTimeoutException {
        /* Create packet for receiving the reply */
        byte[] receiveData = new byte[MAX_PING_MESSAGE_LENGTH];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        /* Read message from socket. */
        Message reply = null;
        try {
            // Set a socket timeout to handle potential timeouts
            socket.setSoTimeout(1000); // Set timeout to 1 second (adjust as needed)
            socket.receive(receivePacket);
            String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            reply = new Message(receivePacket.getAddress(), receivePacket.getPort(), receivedMessage);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e) {
            System.out.println("Error reading from socket: " + e);
        }

        return reply;
    }
}
