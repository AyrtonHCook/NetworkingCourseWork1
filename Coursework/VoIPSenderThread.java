import java.net.*;
import java.nio.ByteBuffer;


public class VoIPSenderThread implements Runnable {

    // Packet structure constants
    public static final int HEADER_SIZE  = 4;   // 4 bytes for sequence number (int)
    public static final int PAYLOAD_SIZE = AudioLayer.BLOCK_SIZE_BYTES; // 512 bytes
    public static final int PACKET_SIZE  = HEADER_SIZE + PAYLOAD_SIZE;  // 516 bytes total

    // Network config
    private static final int    PORT            = 55555;
    private static final String RECEIVER_IP     = "localhost";

    private AudioLayer     audioLayer;
    private DatagramSocket socket;
    private InetAddress    receiverAddress;
    private boolean        running;

    // Creates a VoIPSenderThread
    public VoIPSenderThread(AudioLayer audioLayer) throws Exception {
        this.audioLayer = audioLayer;

        // Transport Layer: opens an unbound UDP socket
        socket = new DatagramSocket();

        receiverAddress = InetAddress.getByName(RECEIVER_IP);
    }

    // Starts this thread running in the background
    public void start() {
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
    }

    // Signals the sender loop
    public void stop() {
        running = false;
        socket.close();
    }


     // Main sender loop
     //  1. Records a 512-byte audio block from the Audio Layer
     //  2. Builds a 516-byte VoIP packet
     //  3. Sends it via UDP to the receiver
    @Override
    public void run() {
        running = true;
        int sequenceNumber = 0;

        System.out.println("[VoIPSender] Channel 1 sender started. Sending to "
                + RECEIVER_IP + ":" + PORT);

        while (running) {
            try {
                // Audio Layer interface
                byte[] audioBlock = audioLayer.getBlock();  // blocks until 32ms block ready

                // VoIP Layer: builds packet with header
                ByteBuffer packetBuffer = ByteBuffer.allocate(PACKET_SIZE);
                packetBuffer.putInt(sequenceNumber);   // 4-byte sequence number
                packetBuffer.put(audioBlock);          // 512-byte audio payload
                byte[] packetData = packetBuffer.array();

                // Transport Layer interface
                DatagramPacket packet = new DatagramPacket(
                        packetData, packetData.length, receiverAddress, PORT);
                socket.send(packet);

                sequenceNumber++;  // increment for next packet

            } catch (Exception e) {
                if (running) {
                    System.err.println("[VoIPSender] Error: " + e.getMessage());
                }
            }
        }

        System.out.println("[VoIPSender] Sender stopped.");
    }
}
