import java.net.InetAddress;

public class AudioDuplexChannel3 {
    public static void main(String[] args) throws Exception {
        int listenPort = 55555;
        InetAddress targetIp = InetAddress.getByName("localhost");

        ReceiverAudioLayer receiverAudio = new ReceiverAudioLayer();
        SenderAudioLayer senderAudio = new SenderAudioLayer();

        AudioReceiverChannel3 receiver = new AudioReceiverChannel3(receiverAudio, listenPort);
        AudioSenderChannel3 sender = new AudioSenderChannel3(senderAudio, targetIp, listenPort);

        receiver.start();
        sender.start();
    }
}