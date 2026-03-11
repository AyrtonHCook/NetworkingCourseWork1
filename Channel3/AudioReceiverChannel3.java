import java.net.*;
import java.util.Map;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.uea.cmp.voip.DatagramSocket3;

public class AudioReceiverChannel3 implements Runnable {

    private final int listenPort;

    private static final int FRAME_TIME_MS = ReceiverAudioLayer.BLOCK_DURATION_MS;
    private static final int AUDIO_BLOCK_SIZE = ReceiverAudioLayer.BLOCK_SIZE_BYTES;

    private static final int START_BUFFER_SIZE = 12;
    private static final int MAX_LATE_PACKETS = 50;

    private final ReceiverAudioLayer receiverAudio;
    private DatagramSocket receiverSocket;
    private volatile boolean isRunning;

    private final Map<Integer, byte[]> packetBuffer = new ConcurrentHashMap<>();

    private volatile boolean hasStarted;
    private volatile int nextPacketNumber;

    private byte[] previousGoodBlock = new byte[AUDIO_BLOCK_SIZE];
    private int missedPacketsInRow = 0;

    public AudioReceiverChannel3(ReceiverAudioLayer receiverAudio, int listenPort) throws Exception {
        this.receiverAudio = receiverAudio;
        this.listenPort = listenPort;
    }

    public void start() {
        Thread receiverThread = new Thread(this, "VoIPReceiverChannel3");
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    public void stop() {
        isRunning = false;
        if (receiverSocket != null && !receiverSocket.isClosed()) {
            receiverSocket.close();
        }
    }

    @Override
    public void run() {
        isRunning = true;
        hasStarted = false;
        nextPacketNumber = 0;

        try {
            receiverSocket = new DatagramSocket3(listenPort);
        } catch (SocketException e) {
            System.err.println("[Ch3Receiver] Receiver failed: " + e.getMessage());
            return;
        }

        Thread playThread = new Thread(this::playAudioLoop, "Ch3PlayoutLoop");
        playThread.setDaemon(true);
        playThread.start();

        System.out.println("[Ch3Receiver] Channel 3 receiver started. Listening on port " + listenPort);

        byte[] packetData = new byte[2048];
        DatagramPacket receivedPacket = new DatagramPacket(packetData, packetData.length);

        while (isRunning) {
            try {
                receiverSocket.receive(receivedPacket);

                int packetLength = receivedPacket.getLength();
                if (packetLength < 4) continue;

                byte[] audioBlock = new byte[packetLength - 4];
                System.arraycopy(packetData, 4, audioBlock, 0, packetLength - 4);

                int packetNumber = ByteBuffer.wrap(packetData, 0, 4).getInt();

                if (hasStarted && packetNumber < nextPacketNumber - MAX_LATE_PACKETS) {
                    continue;
                }

                packetBuffer.put(packetNumber, audioBlock);

                if (!hasStarted && packetBuffer.size() >= START_BUFFER_SIZE) {
                    hasStarted = true;
                    nextPacketNumber = packetBuffer.keySet().stream().min(Integer::compareTo).orElse(0);

                    System.out.println("[Ch3Receiver] Playout started at seq " + nextPacketNumber
                            + " with startup buffer " + START_BUFFER_SIZE
                            + " frames (" + (START_BUFFER_SIZE * FRAME_TIME_MS) + "ms)");
                }

            } catch (Exception e) {
                if (isRunning) {
                    System.err.println("[Ch3Receiver] Error: " + e.getMessage());
                }
            }
        }

        System.out.println("[Ch3Receiver] Receiver stopped.");
        stop();
    }

    private void playAudioLoop() {
        while (isRunning) {
            long startTime = System.nanoTime();

            if (hasStarted) {
                byte[] currentBlock = packetBuffer.remove(nextPacketNumber);

                if (currentBlock != null) {
                    try {
                        receiverAudio.playBlock(currentBlock);
                        previousGoodBlock = currentBlock;
                        missedPacketsInRow = 0;
                    } catch (Exception e) {
                        System.err.println("[Ch3Receiver] Audio playBlock failed: " + e.getMessage());
                    }
                } else {
                    missedPacketsInRow++;
                    try {
                        if (missedPacketsInRow <= 3) {
                            receiverAudio.playBlock(previousGoodBlock);
                        } else {
                            receiverAudio.playSilence();
                        }
                    } catch (Exception e) {
                        System.err.println("[Ch3Receiver] Concealment failed: " + e.getMessage());
                    }
                }

                nextPacketNumber++;
            }

            long timeTaken = System.nanoTime() - startTime;
            long targetTime = FRAME_TIME_MS * 1_000_000L;
            long sleepTime = targetTime - timeTaken;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1_000_000L, (int) (sleepTime % 1_000_000L));
                } catch (InterruptedException ignored) {}
            }
        }
    }
}