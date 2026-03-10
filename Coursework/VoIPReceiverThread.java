import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;


public class VoIPReceiverThread implements Runnable {

    // Packet structure: must match VoIPSenderThread
    private static final int HEADER_SIZE  = VoIPSenderThread.HEADER_SIZE;
    private static final int PACKET_SIZE  = VoIPSenderThread.PACKET_SIZE;
    private static final int CIPHERTEXT_SIZE = 512;

    // Network config: must match sender
    private static final int PORT = 55555;

    // Timeout: how long to wait for a packet before playing silence.
    // Set to one block duration (32ms) so the audio stays in sync.
    private static final int SOCKET_TIMEOUT_MS = AudioLayer.BLOCK_DURATION_MS;

    private AudioLayer     audioLayer;
    private DatagramSocket socket;
    private volatile boolean        running;

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



     // Main receiver loop.
     //  1. Waits up to 32ms for a UDP packet from the Transport Layer
     //  2. If a packet arrives, gets the sequence number and audio payload
     //  3. If no packet arrives within timeout, plays silence to keep audio timing
     //  4. Passes audio up to the Audio Layer for playback
    @Override
    public void run() {
        running = true;
        int expectedSequenceNumber = 0;
        SecurityLayer sec = new SecurityLayer();
        BigInteger n = new BigInteger("703066526860816942287777156524360518055407827466697888592637417094017290394214927213238702630010019208354126500461658975560802975422333552604204487001201499996009317236258193985719873760718327113990756776696447523712591431252788272680262606911924803725011434820171777624632289386938774482173393778794164294349478880860555882094299434844339731682977360227972993762000396127013057766571910731286146349393646303118375768106973861677576645599805045495129650024373721100949266595114653633990009909378416213797303343051164477280326136547850605219675280215277744315462858943320292282133275247902301017030013814132450843977644732048254142916679187036926338177846683893046819725585536853897655915975768867490106352387440015328384824418344747410570621392546753216416350361125678904879442464799809963796057258725585452176954978699955236746443270354904129250763803886733480347743599216701368701353701879950783046970049431332965560876979487233517420773130930232792627171289405283946719162220636842867130488937138100119791607157664796569868900594871421936223771711984325463868242398692392480097406966398802847857737141999903467547199996345214749749271894271686894987246999553471529584746065712764451499400974385431615784099135827980724502090836917");
        BigInteger d = new BigInteger("122768715891712911600184960850584887447183837794358768894733396420707903493772916475095041166025827545057061258087541775124247818037645683751201857717651860261445147419804671742261291107582900277591440263553628415419791817435294703641499081030563917387567799259686067765327859373241487025252793359545301374559949895670662397038118356536897079350290567319970748441526657205510435831372338471532701509413932409064905203018389747364667090837910934901601594746157634073565518820124389218111626614018441417072742717211308516990342132027001576607625675676085852357388299094364365547350858323343972608433273999251289614393084113838640581358875629514108535339637114044179689271674467376958272554749901855682701414006519938220937389298868728150155869963981233629711377860023969377574809719347124253364036175522878900296879583802486457551695833035379983908461348826282777591895902218685437795130970925959619581622212246935173996151630865616269016308608587803876566399340261062545658526592678888324527947006701700186435195812769238944000122912712472508250013842872534004946892764966610857534291481073164801647645648865781044991039328624931859201146376864219892902383483654102725297947472705510447478772093288755504222290627309971672387409815665");
        BigInteger[] keys = new BigInteger[]{n,d};

        System.out.println("[VoIPReceiver] Channel 1 receiver started. Listening on port " + PORT);

        while (running) {
            try {
                // Transport Layer interface: wait for a VoIP packet
                byte[] buffer = new byte[PACKET_SIZE];
                DatagramPacket udpPacket = new DatagramPacket(buffer, PACKET_SIZE);
                socket.receive(udpPacket);  // blocks until packet arrives or timeout

                ByteBuffer packetBuffer = ByteBuffer.wrap(buffer);

                byte[] header = new byte[HEADER_SIZE];
                packetBuffer.get(header);

                ByteBuffer headerReader = ByteBuffer.wrap(header);
                int receivedSeqNo = headerReader.getInt();

                byte[] encryptedBlock = new byte[CIPHERTEXT_SIZE];
                packetBuffer.get(encryptedBlock);

                int receivedCheck = packetBuffer.getInt();

                int expectedCheck = computeCheck(header, encryptedBlock);

                if (receivedCheck != expectedCheck) {
                    System.out.println("[VoIPReceiver] Packet failed check. Dropping.");
                    continue;
                }

                // Detect missing packets (Channel 1: for logging only; no mitigation needed)
                if (receivedSeqNo > expectedSequenceNumber) {
                    System.out.println("[VoIPReceiver] Warning: expected seq "
                            + expectedSequenceNumber + " but got " + receivedSeqNo
                            + " , " + (receivedSeqNo - expectedSequenceNumber) + " packet(s) missing.");
                } else if (receivedSeqNo < expectedSequenceNumber) {
                    System.out.println("[VoIPReceiver] Warning: received old or out-of-order packet. Expected "
                            + expectedSequenceNumber + " but got " + receivedSeqNo + ".");
                }
                expectedSequenceNumber = receivedSeqNo + 1;

                byte[] decrypted = sec.decryption(encryptedBlock, keys);
                System.out.println("decrypted length = " + decrypted.length);

                // Audio Layer interface: play the received audio
                audioLayer.playBlock(decrypted);

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
