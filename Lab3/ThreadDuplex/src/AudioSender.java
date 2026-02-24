import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class AudioSender implements Runnable{

    public DatagramSocket sending_socket;

    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        //initialise variables
        int PORT = 55555;
        InetAddress clientIP = null;
        AudioRecorder recorder = null;
        byte[] block = null;
        short authenticationKey = 10;

        try {
            // get client IP address
            clientIP = InetAddress.getByName("");
        } catch (UnknownHostException e) {
            System.out.println("Errorr: Counld not find client IP.");
            System.exit(0);
        }

        try{
            // create socket
            sending_socket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Error: Unable to open socket.");
            System.exit(0);
        }

        try {
            // create audio recorder
            recorder = new AudioRecorder();
        } catch (LineUnavailableException e) {
            System.out.println("Error: Unable to open AudioRecorder.");
            System.exit(0);
        }

        int key = 15;
        System.out.println("Session start.");
        while(true){
            try{
                // get payload of the packet
                block = recorder.getBlock();
                ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
                ByteBuffer plainText = ByteBuffer.wrap(block);
                for( int j = 0; j < block.length/4; j++) {
                    int fourByte = plainText.getInt();
                    fourByte = fourByte ^ key; // XOR operation with key
                    unwrapEncrypt.putInt(fourByte);
                }
                byte[] encryptedBlock = unwrapEncrypt.array();

                // get header
                ByteBuffer VoIPpacket = ByteBuffer.allocate(514);
                VoIPpacket.putShort(authenticationKey); // place the authentication key in the header
                VoIPpacket.put(encryptedBlock); // place the payload of the packet
                byte[] sendingPacket = VoIPpacket.array();

                DatagramPacket packet = new DatagramPacket(sendingPacket, sendingPacket.length, clientIP, PORT);
                sending_socket.send(packet);
            } catch (IOException e) {
                System.out.println("Error: IO exception occurred.");
                System.exit(0);
            }
        }
    }

}
