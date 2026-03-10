package NetworkingCourseWork1.Coursework;

import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;


public class VoIPSenderThread implements Runnable {

    // Packet structure constants
    public static final int HEADER_SIZE  = 4;   // 4 bytes for sequence number (int)
    public static final int CIPHERTEXT_SIZE = 512;
    public static final int CHECK_SIZE = 4;
    public static final int PACKET_SIZE  = HEADER_SIZE + CIPHERTEXT_SIZE + CHECK_SIZE; 

    // Network config
    private static final int    PORT            = 55555;
    private static final String RECEIVER_IP     = "localhost";

    private AudioLayer     audioLayer;
    private DatagramSocket socket;
    private InetAddress    receiverAddress;
    private volatile boolean        running;

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

    private static final byte[] SHARED_SECRET = "secret".getBytes();

    public static int computeCheck(byte[] header, byte[] payload) {
        int hash = 17;

        for (byte b : header) {
            hash = 31 * hash + (b & 0xFF);
        }

        for (byte b : payload) {
            hash = 31 * hash + (b & 0xFF);
        }

        for (byte b : SHARED_SECRET) {
            hash = 31 * hash + (b & 0xFF);
        }

        return hash;
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
        SecurityLayer sec = new SecurityLayer();
        //BigInteger[] keys = sec.genKey();
        BigInteger n = new BigInteger("703066526860816942287777156524360518055407827466697888592637417094017290394214927213238702630010019208354126500461658975560802975422333552604204487001201499996009317236258193985719873760718327113990756776696447523712591431252788272680262606911924803725011434820171777624632289386938774482173393778794164294349478880860555882094299434844339731682977360227972993762000396127013057766571910731286146349393646303118375768106973861677576645599805045495129650024373721100949266595114653633990009909378416213797303343051164477280326136547850605219675280215277744315462858943320292282133275247902301017030013814132450843977644732048254142916679187036926338177846683893046819725585536853897655915975768867490106352387440015328384824418344747410570621392546753216416350361125678904879442464799809963796057258725585452176954978699955236746443270354904129250763803886733480347743599216701368701353701879950783046970049431332965560876979487233517420773130930232792627171289405283946719162220636842867130488937138100119791607157664796569868900594871421936223771711984325463868242398692392480097406966398802847857737141999903467547199996345214749749271894271686894987246999553471529584746065712764451499400974385431615784099135827980724502090836917");
        BigInteger E = new BigInteger("65537");
        BigInteger[] keys = new BigInteger[]{n, E};

        while (running) {
            try {
                // Audio Layer interface
                byte[] audioBlock = audioLayer.getBlock();  // blocks until 32ms block ready
                byte[] encrpyted = sec.encryption(audioBlock, new BigInteger[]{keys[0], keys[1]}); // encryption

                if (encrpyted.length != CIPHERTEXT_SIZE) {
                throw new IllegalStateException(
                    "Encrypted block length was " + encrpyted.length +
                    ", expected " + CIPHERTEXT_SIZE
                );
                }

                ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_SIZE);
                headerBuffer.putInt(sequenceNumber);
                byte[] header = headerBuffer.array();

                int check = computeCheck(header, encrpyted);


                ByteBuffer packetBuffer = ByteBuffer.allocate(PACKET_SIZE);
                packetBuffer.put(header);
                packetBuffer.put(encrpyted);
                packetBuffer.putInt(check);
                byte[] packetData = packetBuffer.array();



                // Transport Layer interface
                DatagramPacket packet = new DatagramPacket(
                        packetData, packetData.length, receiverAddress, PORT);
                // test for authentication should drop all receiving packets if uncommented
                //packetData[10] ^= 1;
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
