import java.net.*;
import java.nio.ByteBuffer;


public class VoIPReceiverThread implements Runnable {

    // Packet structure: must match VoIPSenderThread
    private static final int HEADER_SIZE  = VoIPSenderThread.HEADER_SIZE;
    private static final int PAYLOAD_SIZE = VoIPSenderThread.PAYLOAD_SIZE;
    private static final int PACKET_SIZE  = VoIPSenderThread.PACKET_SIZE;

    // Network config: must match sender
    private static final int PORT = 55555;

    // Timeout: how long to wait for a packet before playing silence.
    // Set to one block duration (32ms) so the audio stays in sync.
    private static final int SOCKET_TIMEOUT_MS = AudioLayer.BLOCK_DURATION_MS;

    private AudioLayer     audioLayer;
    private DatagramSocket socket;
    private boolean        running;

    // Creates a VoIPReceiverThread
    public VoIPReceiverThread(AudioLayer audioLayer) throws Exception {
        this.audioLayer = audioLayer;

        // Transport Layer: opens a receiving socket bound to PORT
        socket = new DatagramSocket(PORT);

        // Set timeout so receive() doesn't block indefinitely
        socket.setSoTimeout(SOCKET_TIMEOUT_MS);
    }

    // Starts this thread running in the background
    public void start() {
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
    }

    // Signals the receiver loop to make the stop clean.
    public void stop() {
        running = false;
        socket.close();
    }

     // Main receiver loop.
     //  1. Waits up to 32ms for a UDP packet from the Transport Layer
     //  2. If a packet arrives, gets the sequence number and audio payload
     //  3. If no packet arrives within timeout, plays silence to keep audio timing
     //  4. Passes audio up to the Audio Layer for playback
    @Override
    public void run() {
        running = true;
        int expectedSequenceNumber = 0;

        System.out.println("[VoIPReceiver] Channel 1 receiver started. Listening on port " + PORT);

        while (running) {
            try {
                // Transport Layer interface: wait for a VoIP packet
                byte[] buffer = new byte[PACKET_SIZE];
                DatagramPacket udpPacket = new DatagramPacket(buffer, PACKET_SIZE);
                socket.receive(udpPacket);  // blocks until packet arrives or timeout

                // VoIP Layer: parse the packet
                ByteBuffer packetBuffer = ByteBuffer.wrap(buffer);

                // Extract the 4-byte sequence number from the header
                int receivedSeqNo = packetBuffer.getInt();

                // Detect missing packets (Channel 1: for logging only; no mitigation needed)
                if (receivedSeqNo != expectedSequenceNumber) {
                    System.out.println("[VoIPReceiver] Warning: expected seq "
                            + expectedSequenceNumber + " but got " + receivedSeqNo
                            + " — " + (receivedSeqNo - expectedSequenceNumber) + " packet(s) lost.");
                }
                expectedSequenceNumber = receivedSeqNo + 1;

                // Extract the 512-byte audio payload
                byte[] audioBlock = new byte[PAYLOAD_SIZE];
                packetBuffer.get(audioBlock);

                // Audio Layer interface: play the received audio
                audioLayer.playBlock(audioBlock);

            } catch (SocketTimeoutException e) {
                // If no packet arrived within 32ms play silence to keep audio timing
                // This keeps the playout buffer moving
                try {
                    audioLayer.playSilence();
                } catch (Exception ex) {
                    System.err.println("[VoIPReceiver] Error playing silence: " + ex.getMessage());
                }
                expectedSequenceNumber++; // assume one packet was missed

            } catch (Exception e) {
                if (running) {
                    System.err.println("[VoIPReceiver] Error: " + e.getMessage());
                }
            }
        }

        System.out.println("[VoIPReceiver] Receiver stopped.");
    }
}
