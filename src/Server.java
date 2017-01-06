import java.net.*;
import java.io.*;

/**
 * Echo Server.
 * A simple server echoing messages sent with the emulated protocol hierarchy
 * back to the source.
 */
public class Server {

    private NetworkLayer networkLayer;

    public Server(String[] argv) throws SocketException {
        int localPort = Integer.parseInt(argv[0]);
        networkLayer = new NetworkLayer(localPort);
    }

    /**
     * Serve messages.
     * Accept messages from the emulated network layer and send DataPackets back to the source.
     */
    public void serve() {
        while (true) {
            try {
                IpPacket ipPacket = networkLayer.getPacket();
                System.out.println("Received packet from " 
                        + ipPacket.getNextHopIp().getHostAddress() + "/" + ipPacket.getNextHopPort());
                System.out.println("  " + ipPacket);
                if (ipPacket.getType() == IpPacket.Header.Data) {
                    DataPacket dataPacket = ipPacket.getDataPacket();
                    Inet6Address swap = ipPacket.getSourceAddress();
                    ipPacket.setSourceAddress(ipPacket.getDestinationAddress());
                    ipPacket.setDestinationAddress(swap);
                    networkLayer.sendPacket(ipPacket);
                }
            } catch (IOException e) {
                System.err.println("Error echoing package: " + e.toString());
                return;
            }
        }
    }

    public static void main(String[] argv) {
        if (argv.length != 1) {
            System.out.println("Usage: Server <local port>");
            System.out.println("    local port");
            System.out.println("        - the port on which the server listens for packets");
            return;
        }

        Server server;

        try {
            server = new Server(argv);
        } catch (SocketException e) {
            System.err.println("Error setting up server: " + e.toString());
            return;
        }

        server.serve();
    }
}
