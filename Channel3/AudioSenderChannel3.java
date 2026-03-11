import java.net.*;
import java.nio.ByteBuffer;

public class AudioSenderChannel3 implements Runnable {

    private final InetAddress targetIp;
    private final int targetPort;
    private DatagramSocket senderSocket;

    private volatile boolean isRunning = true;
    private int packetNumber = 0;

    private final SenderAudioLayer senderAudio;

    public AudioSenderChannel3(SenderAudioLayer senderAudio, InetAddress targetIp, int targetPort) {
        this.senderAudio = senderAudio;
        this.targetIp = targetIp;
        this.targetPort = targetPort;
    }

    public void start() {
        new Thread(this, "AudioSenderChannel3").start();
    }

    public void stop() {
        isRunning = false;
        if (senderSocket != null && !senderSocket.isClosed()) {
            senderSocket.close();
        }
    }

    @Override
    public void run() {
        try {
            senderSocket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Sender init failed: " + e.getMessage());
            return;
        }

        while (isRunning) {
            try {
                byte[] audioBlock = senderAudio.getBlock();

                ByteBuffer buffer = ByteBuffer.allocate(4 + audioBlock.length);
                buffer.putInt(packetNumber);
                buffer.put(audioBlock);

                byte[] fullPacket = buffer.array();

                DatagramPacket sendPacket = new DatagramPacket(fullPacket, fullPacket.length, targetIp, targetPort);
                senderSocket.send(sendPacket);

                packetNumber++;

            } catch (Exception e) {
                System.out.println("Sender IO failed: " + e.getMessage());
                break;
            }
        }

        stop();
    }
}