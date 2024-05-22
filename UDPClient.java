import java.net.*;

public class UDPClient extends PingClient {
    /** Host to ping */
    String remoteHost;

    /** Port number of remote host */
    int remotePort;

    /** How many pings to send */
    static final int NUM_PINGS = 10;

    /** How many reply pings have we received */
    int numReplies = 0;

    /** Create an array for holding replies and RTTs */
    long[] rttArray = new long[NUM_PINGS];

    /* Send our own pings at least once per second. If no replies received
     within 5 seconds, assume ping was lost. */
    /** 1 second timeout for waiting replies */
    static final int TIMEOUT = 1000;

    /** 5 second timeout for collecting pings at the end */
    static final int REPLY_TIMEOUT = 5000;

    /** constructor **/
    public UDPClient(String host, int port) {
        this.remoteHost = host;
        this.remotePort = port;
    }

    public static void main(String args[]) {
        String host = null;
        int port = 0;

        /* Parse host and port number from command line */
        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Need two arguments: remoteHost remotePort");
            System.exit(-1);
        } catch (NumberFormatException e) {
            System.out.println("Please give port number as an integer.");
            System.exit(-1);
        }

        System.out.println("Contacting host " + host + " at port " + port);

        // Create a new UDPClient object and run it
        UDPClient client = new UDPClient(host, port);
        client.run();
    }

    public void run() {
        /* Create socket. We do not care which local port we use. */
        createSocket();
        try {
            socket.setSoTimeout(TIMEOUT);
        } catch (SocketException e) {
            System.out.println("Error setting timeout TIMEOUT: " + e);
        }
        for (int i = 0; i < NUM_PINGS; i++) {
            /* Message we want to send to the server is just the current time. */
            long currentTime = System.currentTimeMillis();
            String messageContent = String.valueOf(currentTime);
            /* Send ping to recipient */
            try {
                InetAddress hostAddress = InetAddress.getByName(remoteHost);
                Message ping = new Message(hostAddress, remotePort, messageContent);
                sendPing(ping);
            } catch (UnknownHostException e) {
                System.out.println("Cannot find host: " + e);
            }

            /* Read the reply by getting the received ping message */
            try {
                Message reply = receivePing();
                handleReply(reply);
            } catch (SocketTimeoutException e) {
                /* Reply did not arrive. Do nothing for now. Figure out lost pings later. */
            }
        }

        // Check for missing replies
        try {
            socket.setSoTimeout(REPLY_TIMEOUT);
        } catch (SocketException e) {
            System.out.println("Error setting timeout REPLY_TIMEOUT: " + e);
        }

        while (numReplies < NUM_PINGS) {
            try {
                Message reply = receivePing();
                handleReply(reply);
            } catch (SocketTimeoutException e) {
                /* Nothing coming our way apparently. Exit loop. */
                break;
            }
        }

        // Print statistics
        for (int i = 0; i < NUM_PINGS; i++) {
            System.out.println("Ping " + i + ": RTT=" + rttArray[i] + " ms");
        }
    }

    private void handleReply(Message reply) {
        /* Calculate RTT and store it in the rtt-array. */
        // Calculates by subtracting the sent time from the current time
        long currentTime = System.currentTimeMillis();
        long sentTime = Long.parseLong(reply.getContents());
        long rtt = currentTime - sentTime;
        rttArray[numReplies] = rtt;
        System.out.println("Received message from " + reply.getIP() + ":" + reply.getPort());
        System.out.println("PING " + numReplies + " " + sentTime);

        numReplies++;
    }
}
