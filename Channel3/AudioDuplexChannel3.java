
import java.net.InetAddress;

public class AudioDuplexChannel3 {
    public static void main(String[] args) throws Exception {
        int port = 55555;
        InetAddress ip = InetAddress.getByName("localhost");

        ReceiverAudioLayer rxAudio = new ReceiverAudioLayer();
        SenderAudioLayer txAudio = new SenderAudioLayer();

        AudioReceiverChannel3 rx = new AudioReceiverChannel3(rxAudio, port);
        AudioSenderChannel3 tx = new AudioSenderChannel3(txAudio, ip, port);

        rx.start();
        tx.start();
    }
}