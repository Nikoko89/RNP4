import java.net.*; 
import java.lang.*;
import java.io.*;
import java.util.*;

/**
 * A base packet of the emulated protocol hierarchy.
 *
 * This packet format has the function of IPv6 in the protocol
 * hierarchy.
 *
 * @author  Christian Keil
 * @version 1.0
 */
public class IpPacket {
    public final static int HEADER_LEN = 34;
    public final static int MAX_SIZE = 1000;

    /**
     * The modelled types of headers.
     *
     * These types of data can be embedded in an IpPacket.
     */
    public enum Header {
        Data, Control
    };

    private Inet6Address source;
    private Inet6Address destination;
    private int hopLimit;
    private Header nextHeader;
    private byte[] payload;

    private Inet6Address nextHopIp;
    private int nextHopPort;

    /**
     * Construct a base packet with given parameters.
     *
     * @param source    the source address for the packet
     * @param destination   the destination address for the
     * packet
     * @param hopLimit  the hop limit for the packet. Has to
     * be between 0 and 255.
     * @param nextHopIp the address of the next hop on the
     * route of the packet to its destination 
     * @param nextHopPort   the port the next hop is listening on
     */
    public IpPacket(Inet6Address source, Inet6Address destination, int hopLimit, Inet6Address nextHopIp, int nextHopPort) {
        this.source = source;
        this.destination = destination;
        if ((hopLimit < 0) || (hopLimit > 255)) {
            throw new IllegalArgumentException("Hoplimit must be between 0 and 255");
        }
        this.hopLimit = hopLimit;
        this.nextHopIp = nextHopIp;
        this.nextHopPort = nextHopPort;
    }

    /**
     * Constructs a base packet to be sent from the local host.
     *
     * Constructs a packet with the source address set to the
     * IPv6 address of the local host.  
     *
     * @param destination   the destination address for the
     * packet
     * @param hopLimit  the hop limit for the packet. Has to
     * be between 0 and 255.
     * @param nextHopIp the address of the next hop on the
     * route of the packet to its destination 
     * @param nextHopPort   the port the next hop is listening on
     */
    public IpPacket(Inet6Address destination, int hopLimit, Inet6Address nextHopIp, int nextHopPort) throws UnknownHostException {
        this((Inet6Address) Inet6Address.getByName("::1"), destination, hopLimit, nextHopIp, nextHopPort);
        source = (Inet6Address) Inet6Address.getLocalHost();
    }

    /**
     * Constructs a base packet from a binary representation.
     *
     * @param packetData    the binary representation of the packet
     * @param packetLength  the length of the representation
     * in the packetData buffer
     */
    public IpPacket(byte[] packetData, int packetLength) throws UnknownHostException {
        if (packetLength > MAX_SIZE) {
            throw new IllegalArgumentException("IpPackets cannot be larger than 1000 bytes. Asked for " + packetLength + " bytes.");
        }
        ByteArrayInputStream in = new ByteArrayInputStream(packetData);
        byte[] data = new byte[16];
        in.read(data, 0, 16);
        source = (Inet6Address) Inet6Address.getByAddress(data);
        in.read(data, 0, 16);
        destination = (Inet6Address) Inet6Address.getByAddress(data);
        hopLimit = in.read();
        nextHeader = Header.values()[in.read()];
        payload = new byte[packetLength - HEADER_LEN];
        in.read(payload, 0, payload.length);
    }

    /**
     * Constructs a base packet from a binary representation.
     *
     * Constructs a base packet using the whole binary buffer.
     *
     * @param packetData    the binary representation of the packet
     */
    public IpPacket(byte[] packetData) throws UnknownHostException {
        this(packetData, packetData.length);
    }

    /**
     * Constructs a base packet from the payload of a DatagramPacket.
     *
     * Constructs the base packet and sets the nextHopIp and
     * nextHopPort to the IP and port this packet was received
     * from.
     *
     * @param packet    the DatagramPacket to extract the
     * payload from
     */
    public IpPacket(DatagramPacket packet) throws UnknownHostException {
        this(packet.getData(), packet.getLength());
        nextHopIp = (Inet6Address) packet.getAddress();
        nextHopPort = packet.getPort();
    }

    /**
     * Gets the source address.
     */
    public Inet6Address getSourceAddress() {
        return source;
    }

    /**
     * Sets the source address.
     */
    public void setSourceAddress(Inet6Address source) {
        this.source = source;
    }

    /**
     * Gets the destination address.
     */
    public Inet6Address getDestinationAddress() {
        return destination;
    }

    /**
     * Sets the destination address.
     */
    public void setDestinationAddress(Inet6Address destination) {
        this.destination = destination;
    }

    /**
     * Gets the hop limit.
     */
    public int getHopLimit() {
        return hopLimit;
    }

    /**
     * Sets the hop limit.
     *
     * @param hopLimit  the hop limit. Has to be between 0 and 255.
     */
    public void setHopLimit(int hopLimit) {
        if ((hopLimit < 0) || (hopLimit > 255)) {
            throw new IllegalArgumentException("Hoplimit must be between 0 and 255");
        }
        this.hopLimit = hopLimit;
    }

    /**
     * Gets the type of the embedded packet.
     */
    public Header getType() {
        return nextHeader;
    }

    /**
     * Gets the embedded data packet.
     *
     * Gets the embedded data packet if the embedded packet is
     * a data packet.
     *
     * @throws NoSuchElementException   if the embedded packet
     * is not a data packet.
     */
    public DataPacket getDataPacket() {
        if (nextHeader != Header.Data) {
            throw new NoSuchElementException();
        }
        return new DataPacket(payload);
    }

    /**
     * Gets the embedded control packet.
     *
     * Gets the embedded control packet if the embedded packet
     * is a control packet.
     *
     * @throws NoSuchElementException   if the embedded packet
     * is not a control packet.
     */
    public ControlPacket getControlPacket() {
        if (nextHeader != Header.Control) {
            throw new NoSuchElementException();
        }
        return new ControlPacket(payload);
    }

    /**
     * Set a data payload.
     *
     * Sets the next header type to data and stores the binary
     * representation of the data packet as the payload.
     *
     * @param payload   the binary representation of the data
     * packet
     */
    public void setDataPayload(byte[] payload) {
        int maxPayloadSize = MAX_SIZE - HEADER_LEN;
        if (payload.length > maxPayloadSize) {
            throw new IllegalArgumentException("IpPackets cannot store payloads larger than " + maxPayloadSize +"  bytes. Asked for " + payload.length + " bytes.");
        }
        nextHeader = Header.Data;
        this.payload = new byte[payload.length];
        System.arraycopy(payload, 0, this.payload, 0, payload.length);
    }

    /**
     * Sets a control payload.
     *
     * Sets the next header type to control and stores the
     * binary representation of the control packet as the
     * payload.
     *
     * @param payload   the binary representation of the
     * control packet
     */
    public void setControlPayload(byte[] payload) {
        int maxPayloadSize = MAX_SIZE - HEADER_LEN;
        if (payload.length > maxPayloadSize) {
            throw new IllegalArgumentException("IpPackets cannot store payloads larger than " + maxPayloadSize +"  bytes. Asked for " + payload.length + " bytes.");
        }
        nextHeader = Header.Control;
        this.payload = new byte[payload.length];
        System.arraycopy(payload, 0, this.payload, 0, payload.length);
    }

    /**
     * Gets the address of the next hop.
     */
    public Inet6Address getNextHopIp() {
        return nextHopIp;
    }

    /**
     * Sets the address of the next hop.
     */
    public void setNextHopIp(Inet6Address Ip) {
        nextHopIp = Ip;
    }

    /**
     * Gets the next hop port.
     */
    public int getNextHopPort() {
        return nextHopPort;
    }

    /**
     * Sets the next hop port.
     */
    public void setNextPort(int port) {
        nextHopPort = port;
    }

    /**
     * Gets the binary representation of the packet.
     *
     * @return  the binary representation
     */
    public byte[] getBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = source.getAddress();
        out.write(data, 0, data.length);
        data = destination.getAddress();
        out.write(data, 0, data.length);
        out.write(hopLimit);
        out.write(nextHeader.ordinal());
        out.write(payload, 0, payload.length);
        return out.toByteArray();
    }

    /**
     * Converts the packet to a printable representation.
     */
    @Override
    public String toString() {
        String payloadString = "";
        switch (nextHeader) {
            case Data:
                payloadString = this.getDataPacket().toString();
                break;
            case Control:
                payloadString = this.getControlPacket().toString();
                break;
        }
        return "[src=" + source.getHostAddress() + "|"
            + "dst=" + destination.getHostAddress() + "|"
            + "HL=" + hopLimit + "|"
            + nextHeader + "|"
            + "len=" + payload.length + "]"
            + "/" + payloadString;
    }
}
