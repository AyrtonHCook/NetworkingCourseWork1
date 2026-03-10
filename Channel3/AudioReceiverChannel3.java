import java.net.*;
import java.util.Map;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.uea.cmp.voip.DatagramSocket3;

public class AudioReceiverChannel3 implements Runnable {

    private final int port;

    private static final int FRAME_MS = ReceiverAudioLayer.BLOCK_DURATION_MS;
    private static final int BLOCK_SIZE = ReceiverAudioLayer.BLOCK_SIZE_BYTES;

    private static final int STARTUP_BUFFER_FRAMES = 12; 
    private static final int MAX_LATE_FRAMES = 50;       

    private final ReceiverAudioLayer audioLayer;
    private DatagramSocket socket;
    private volatile boolean running;


    // Stores packets by sequence number so packets can be played in the right order
    private final Map<Integer, byte[]> buffer = new ConcurrentHashMap<>();
    
    private volatile boolean started; //have we filled the buffer enough to begin playing sound yet
    private volatile int expectedSeq; //the next packet number we want to play

    private byte[] lastGood = new byte[BLOCK_SIZE]; // lastGood stores the last valid audio block so we can replay it
    private int missingStreak = 0; 

    public AudioReceiverChannel3(ReceiverAudioLayer audioLayer, int port) throws Exception {
        this.audioLayer = audioLayer;
        this.port = port;
    }

    public void start() {
        Thread t = new Thread(this, "VoIPReceiverChannel3");
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) socket.close();
    }

    @Override
    public void run() {
        running = true;
        started = false;
        expectedSeq = 0;


        try {
            socket = new DatagramSocket3(port);
        } catch (SocketException e) {
            System.err.println("[Ch3Receiver] Receiver failed: " + e.getMessage());
            return;
        }
        
        //seperate thread for playout 
        //one keeps receiving packets other plays them 
        Thread playout = new Thread(this::playoutLoop, "Ch3PlayoutLoop");
        playout.setDaemon(true);
        playout.start();

        System.out.println("[Ch3Receiver] Channel 3 receiver started. Listening on port " + port);

        byte[] recvBuf = new byte[2048];
        DatagramPacket udpPacket = new DatagramPacket(recvBuf, recvBuf.length);

        while (running) {
            try {
                socket.receive(udpPacket);

                int n = udpPacket.getLength();
                if (n < 4) continue;

                byte[] audioData = new byte[n - 4];
                System.arraycopy(recvBuf, 4, audioData, 0, n - 4);

                int seq = ByteBuffer.wrap(recvBuf, 0, 4).getInt();

                if (started && seq < expectedSeq - MAX_LATE_FRAMES) {
                    continue;
                }

                buffer.put(seq, audioData);

                if (!started && buffer.size() >= STARTUP_BUFFER_FRAMES) {
                    started = true;
                    expectedSeq = buffer.keySet().stream().min(Integer::compareTo).orElse(0);

                    System.out.println("[Ch3Receiver] Playout started at seq " + expectedSeq
                            + " with startup buffer " + STARTUP_BUFFER_FRAMES
                            + " frames (" + (STARTUP_BUFFER_FRAMES * FRAME_MS) + "ms)");
                }

            } catch (Exception e) {
                if (running) {
                    System.err.println("[Ch3Receiver] Error: " + e.getMessage());
                }
            }
        }

        System.out.println("[Ch3Receiver] Receiver stopped.");
        stop();
    }

    private void playoutLoop() {
        while (running) {
            long t0 = System.nanoTime();

            if (started) { //get expected packet and remove it from the buffer
                byte[] frame = buffer.remove(expectedSeq);
 
                if (frame != null) {
                    try {
                        audioLayer.playBlock(frame);
                        lastGood = frame;
                        missingStreak = 0;
                    } catch (Exception e) {
                        System.err.println("[Ch3Receiver] Audio playBlock failed: " + e.getMessage());
                    }
                } else { // packet missing replay good packets a few times then go silent 
                    missingStreak++;
                    try {
                        if (missingStreak <= 3) {
                            audioLayer.playBlock(lastGood);
                        } else {
                            audioLayer.playSilence();
                        }
                    } catch (Exception e) {
                        System.err.println("[Ch3Receiver] Concealment failed: " + e.getMessage());
                    }
                }
                // go next seq number whether packet arrives or not 
                expectedSeq++;
            }

            long elapsed = System.nanoTime() - t0;
            long target = FRAME_MS * 1_000_000L;
            long sleepNs = target - elapsed;
            // sleep for the reset of 32 ms 
            if (sleepNs > 0) {
                try {
                    Thread.sleep(sleepNs / 1_000_000L, (int) (sleepNs % 1_000_000L));
                } catch (InterruptedException ignored) {}
            }
        }
    }
}