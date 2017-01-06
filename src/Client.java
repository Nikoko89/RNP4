import java.net.*;
import java.io.*;

/**
 * A simple client to send packets with the emulated protocol hierarchy.
 *
 * @author  Christian Keil
 * @version 1.0
 */
public class Client {

    private NetworkLayer networkLayer;

    private IpPacket.Header packetType;
    private ControlPacket.Type controlType;
    private byte[] data;
    private Inet6Address echoServerIP;
    private Inet6Address nextHopIp;
    private int nextHopPort;
    private int hopLimit;

    /**
     * Constructs a client for the given destination and next hop.
     *
     * Prepares a client to send the specified packet to the echo server via
     * the given next hop.
     *
     * @param parameters    the parameters for the package transmission.
     * Specifies in order: 
     * - the type of the packet to send
     * - the payload of the packet to send. Specifies the string for a data
     *   packet and the type for a control packet
     * - the address of the destination echo server 
     * - the local UDP port to use for communication
     * - the address of the next hop along the path to the server
     * - the UDP port the next hop is listening on
     * - the hop limit to set for the sent packet
     */
    public Client(String[] parameters) throws SocketException, UnknownHostException {
        packetType = IpPacket.Header.valueOf(parameters[0]);
        switch (packetType) {
            case Data:
                data = parameters[1].getBytes();
                break;
            case Control:
                controlType = ControlPacket.Type.valueOf(parameters[1]);
        }
        echoServerIP = (Inet6Address) InetAddress.getByName(parameters[2]);
        nextHopIp = (Inet6Address) InetAddress.getByName(parameters[4]);
        nextHopPort = Integer.parseInt(parameters[5]);
        hopLimit = Integer.parseInt(parameters[6]);

        int localPort = Integer.parseInt(parameters[3]);
        networkLayer = new NetworkLayer(localPort);
    }

    /**
     * Sends a message and reveices a response for data packets.
     */
    public void sendAndReceiveMessage() throws IOException {
        sendMessage();
        if (packetType == IpPacket.Header.Data) {
            receiveMessage();
        }
    }

    /**
     * Sends a message to the emulated network.
     */
    private void sendMessage() throws IOException {
        IpPacket ipPacket = new IpPacket(echoServerIP, hopLimit, nextHopIp, nextHopPort);
        switch (packetType) {
            case Data:
                DataPacket dataPacket = new DataPacket(data);
                ipPacket.setDataPayload(dataPacket.getBytes());
                break;
            case Control:
                ControlPacket controlPacket = new ControlPacket(controlType, new byte[0]);
                ipPacket.setControlPayload(controlPacket.getBytes());
        }
        networkLayer.sendPacket(ipPacket);
    }

    /**
     * Receives a response from the emulated network and prints its content.
     */
    private void receiveMessage() throws IOException {
        IpPacket ipPacket = networkLayer.getPacket();
        System.out.println("Received packet from " 
                + ipPacket.getNextHopIp().getHostAddress() + "/" + ipPacket.getNextHopPort());
        System.out.println("  " + ipPacket);
    }

    public static void main(String[] argv) {
        // Java prefers IPv4 by default. To find our local IP address we have
        // to force it to prefer IPv6.
        System.getProperties().setProperty("java.net.preferIPv6Addresses", "true");

        if (argv.length != 7) {
            System.out.println("Usage: Client <packet type> <packet data> <echo server IP> <local port> <next hop IP> <next hop port> <HL>");
            System.out.println("    packet type");
            System.out.println("        - type of packet to send: data or control");
            System.out.println("    packet data");
            System.out.println("        - for data packets: string to send; for control packets: type of control packet");
            System.out.println("    echo server IP");
            System.out.println("        - IP of the echo server, this IP is the one used for routing");
            System.out.println("    local port");
            System.out.println("        - the port on which the client listens for packets");
            System.out.println("    next hop IP");
            System.out.println("        - IP of the next hop along the path to the server");
            System.out.println("    next hop port");
            System.out.println("        - Port on which the next hop (Router) listens for packets");
            System.out.println("    HL  - Hop Limit for the packet sent");
            return;
        }

        Client client;
        try {
            client = new Client(argv);
        } catch (SocketException|UnknownHostException e) {
            System.err.println("Error setting up client: " + e.toString());
            return;
        }

        try {
            client.sendAndReceiveMessage();
        } catch (IOException e) {
            System.err.println("Error sending or receiving message: " + e.toString());
            return;
        }
    }
}
