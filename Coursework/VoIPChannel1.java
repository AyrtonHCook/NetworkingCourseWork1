public class VoIPChannel1 {

    public static void main(String[] args) throws Exception {

        System.out.println("=== VoIP Channel 1 (DatagramSocket - ideal channel) ===");
        System.out.println("Starting audio layer...");

        // Audio Layer: shared by both sender and receiver
        AudioLayer audioLayer = new AudioLayer();

        // VoIP Layer: sender and receiver as separate threads
        VoIPSenderThread   sender   = new VoIPSenderThread(audioLayer);
        VoIPReceiverThread receiver = new VoIPReceiverThread(audioLayer);

        // Start both threads: full duplex
        receiver.start();  // start receiver first to minimise chance of missing packets at the start
        Thread.sleep(200); // wait 200ms for receiver to fully initialise to stop packets being missed at the start
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
