import java.io.*; 
import java.net.*;

/**
 * A control message of the emulated protocol hierarchy.
 *
 * This packet format has the function of ICMP in the protocol
 * hierarchy.
 *
 * @author  Christian Keil
 * @version 1.0
 */
public class ControlPacket {

    /**
     * The message type of a control message.
     */
    public enum Type {
        TimeExceeded, DestinationUnreachable;
    }

    private Type type;
    private byte[] payload;

    /**
     * Constructs a control packet with given parameters.
     *
     * @param type  the message type of the control packet
     * @param payload   the payload of the control packet
     */
    public ControlPacket(Type type, byte[] payload) {
        this.type = type;
        this.payload = new byte[payload.length];
        System.arraycopy(payload, 0, this.payload, 0, payload.length);
    }

    /**
     * Constructs a control packet from a binary
     * representation.
     *
     * @param packet    the binary representation of the
     * packet
     */
    public ControlPacket(byte[] packet) {
        ByteArrayInputStream in = new ByteArrayInputStream(packet);
        type = Type.values()[in.read()];
        payload = new byte[packet.length - 1];
        in.read(payload, 0, payload.length);
    }

    /**
     * Gets the length of the packet.
     */
    public int getLen() {
        return 1 + payload.length;
    }

    /**
     * Gets the message type of the packet.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the message type of the packet.
     */
    public void setType(String type) {
        this.type = Type.valueOf(type);
    }

    /**
     * Gets the payload of the packet.
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Sets the payload of the packet.
     */
    public void setPayload(byte[] payload) {
        this.payload = new byte[payload.length];
        System.arraycopy(payload, 0, this.payload, 0, payload.length);
    }

    /**
     * Gets the binary representation of the packet.
     *
     * @return  the binary representation
     */
    public byte[] getBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(type.ordinal());
        out.write(payload, 0, payload.length);
        return out.toByteArray();
    }

    /**
     * Converts the packet to a printable representation.
     */
    @Override
    public String toString() {
        String payloadString = "";
        if (payload.length > 0) {
            try {
                payloadString = new IpPacket(payload).toString();
            } catch (UnknownHostException e) {
            }
        }
        return "[" + type + "|"
            + payloadString;
    }
}
