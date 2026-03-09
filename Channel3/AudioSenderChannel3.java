import java.net.*;
import java.nio.ByteBuffer;

public class AudioSenderChannel3 implements Runnable {

    private final InetAddress destIP;
    private final int port;
    private DatagramSocket sock;

    private volatile boolean running = true;
    private int seq = 0;

    private final SenderAudioLayer audioLayer;

    public AudioSenderChannel3(SenderAudioLayer audioLayer, InetAddress destIP, int port) {
        this.audioLayer = audioLayer;
        this.destIP = destIP;
        this.port = port;
    }

    public void start() {
        new Thread(this, "AudioSenderChannel3").start();
    }

    public void stop() {
        running = false;
        if (sock != null && !sock.isClosed()) sock.close();
    }

    @Override
    public void run() {
        try {
            sock = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Sender init failed: " + e.getMessage());
            return;
        }

        while (running) {
            try {
                byte[] block = audioLayer.getBlock();

                ByteBuffer bb = ByteBuffer.allocate(4 + block.length);
                bb.putInt(seq);
                bb.put(block);

                byte[] payload = bb.array();

                DatagramPacket p = new DatagramPacket(payload, payload.length, destIP, port);
                sock.send(p);

                seq++;

            } catch (Exception e) {
                System.out.println("Sender IO failed: " + e.getMessage());
                break;
            }
        }

        stop();
    }
}