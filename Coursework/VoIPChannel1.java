package NetworkingCourseWork1.Coursework;
public class VoIPChannel1 {

    public static void main(String[] args) throws Exception {

        System.out.println("=== VoIP Channel 1 (DatagramSocket - ideal channel) ===");
        System.out.println("Starting audio layer...");

        // Audio Layer
        AudioLayer audioLayer = new AudioLayer();

        // VoIP Layer
        VoIPSenderThread   sender   = new VoIPSenderThread(audioLayer);
        VoIPReceiverThread receiver = new VoIPReceiverThread(audioLayer);

        // Start both threads: full duplex
        receiver.start();
        Thread.sleep(200);
        sender.start();

        System.out.println("VoIP system is now running. Press ENTER to stop transmitting.");

        // Wait for user to press Enter before shutting down
        System.in.read();

        System.out.println("Shutting down...");
        sender.stop();
        receiver.stop();
        audioLayer.close();

        System.out.println("Done.");
    }
}
