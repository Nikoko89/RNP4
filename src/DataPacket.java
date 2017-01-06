import java.net.*;

/**
 * A data packet of the emulated protocol hierarchy.
 *
 * This packet format has the function of UDP in the protocol
 * hierarchy.
 *
 * @author  Christian Keil
 * @version 1.0
 */
public class DataPacket {
    byte[] payload;

    /**
     * Constructs a data packet with the given payload.
     *
     * @param payload  the payload to use for the packet
     */
    public DataPacket(byte[] payload) {
        this.payload = new byte[payload.length];
        System.arraycopy(payload, 0, this.payload, 0, payload.length);
    }

    /**
     * Gets the length of the packet.
     */
    public int getLen() {
        return payload.length;
    }

    /**
     * Gets the payload of the packet.
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Gets the byte array representation of the packet.
     */
    public byte[] getBytes() {
        return payload;
    }

    /**
     * Converts the packet to a printable representation.
     */
    @Override
    public String toString() {
        return "[" + new String(payload) + "]";
    }
}

